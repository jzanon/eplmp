/*******************************************************************************
 * Copyright (c) 2017 DocDoku.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p>
 * Contributors:
 * DocDoku - initial API and implementation
 *******************************************************************************/

package org.polarsys.eplmp.server;


import org.polarsys.eplmp.core.common.User;
import org.polarsys.eplmp.core.exceptions.*;
import org.polarsys.eplmp.core.meta.InstanceAttribute;
import org.polarsys.eplmp.core.product.*;
import org.polarsys.eplmp.core.services.*;
import org.polarsys.eplmp.i18n.PropertiesLoader;
import org.polarsys.eplmp.server.importers.*;

import javax.annotation.PostConstruct;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.File;
import java.util.*;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Attributes importer
 *
 * @author Elisabel Généreux
 */
@Stateless(name = "ImporterBean")
public class ImporterBean implements IImporterManagerLocal {

    private static final String I18N_CONF = "/org/polarsys/eplmp/server/importers/Importers";

    private static final Logger LOGGER = Logger.getLogger(ImporterBean.class.getName());

    private List<PartImporter> partImporters = new ArrayList<>();
    private List<PathDataImporter> pathDataImporters = new ArrayList<>();

    @Inject
    private IUserManagerLocal userManager;
    @Inject
    private IProductManagerLocal productManager;
    @Inject
    private IProductInstanceManagerLocal productInstanceManager;
    @Inject
    private ILOVManagerLocal lovManager;

    @Inject
    private BeanLocator beanLocator;

    @PostConstruct
    void init() {
        partImporters.addAll(beanLocator.search(PartImporter.class));
        pathDataImporters.addAll(beanLocator.search(PathDataImporter.class));
    }

    @Override
    @Asynchronous
    @FileImport
    public Future<ImportResult> importIntoParts(String workspaceId, File file, String originalFileName, String revisionNote, boolean autoCheckout, boolean autoCheckin, boolean permissiveUpdate) {

        PartImporter selectedImporter = selectPartImporter(file);
        Locale userLocale = getUserLocale(workspaceId);

        Properties properties = PropertiesLoader.loadLocalizedProperties(userLocale, I18N_CONF, ImporterBean.class);

        PartImporterResult partImporterResult;

        if (selectedImporter != null) {
            try {
                partImporterResult = selectedImporter.importFile(userLocale, workspaceId, file, autoCheckout, autoCheckin, permissiveUpdate);
            } catch (PartImporter.ImporterException e) {
                LOGGER.log(Level.SEVERE, null, e);
                List<String> errors = Collections.singletonList(AttributesImporterUtils.createError(properties, "ImporterException", e.getMessage()));
                partImporterResult = new PartImporterResult(file, new ArrayList<>(), errors, null, null, null);
            }
        } else {
            List<String> errors = getNoImporterAvailableError(properties);
            partImporterResult = new PartImporterResult(file, new ArrayList<>(), errors, null, null, null);
        }

        // Maybe should not run the import if parser have some errors
        // ...

        // data update
        ImportResult result = doPartImport(properties, workspaceId, revisionNote, autoCheckout, autoCheckin, permissiveUpdate, partImporterResult);

        return new AsyncResult<>(result);
    }

    @Override
    @Asynchronous
    @FileImport
    public Future<ImportResult> importIntoPathData(String workspaceId, File file, String originalFileName, String revisionNote, boolean autoFreezeAfterUpdate, boolean permissiveUpdate) {
        PathDataImporter selectedImporter = selectPathDataImporter(file);
        Locale userLocale = getUserLocale(workspaceId);
        Properties properties = PropertiesLoader.loadLocalizedProperties(userLocale, I18N_CONF, ImporterBean.class);

        PathDataImporterResult pathDataImporterResult;

        if (selectedImporter != null) {
            pathDataImporterResult = selectedImporter.importFile(getUserLocale(workspaceId), workspaceId, file, autoFreezeAfterUpdate, permissiveUpdate);
        } else {
            List<String> errors = getNoImporterAvailableError(properties);
            pathDataImporterResult = new PathDataImporterResult(file, new ArrayList<>(), errors, null, null, null);
        }

        ImportResult result = new ImportResult(pathDataImporterResult.getImportedFile(), pathDataImporterResult.getWarnings(), pathDataImporterResult.getErrors());

        return new AsyncResult<>(result);
    }


    @Override
    public ImportPreview dryRunImportIntoParts(String workspaceId, File file, String originalFileName, boolean autoCheckout, boolean autoCheckin, boolean permissiveUpdate) throws ImportPreviewException {

        PartImporter selectedImporter = selectPartImporter(file);
        Locale userLocale = getUserLocale(workspaceId);
        Properties properties = PropertiesLoader.loadLocalizedProperties(userLocale, I18N_CONF, ImporterBean.class);

        PartImporterResult partImporterResult;

        if (selectedImporter != null) {
            try {
                partImporterResult = selectedImporter.importFile(userLocale, workspaceId, file, autoCheckout, autoCheckin, permissiveUpdate);
            } catch (PartImporter.ImporterException e) {
                LOGGER.log(Level.SEVERE, null, e);
                List<String> errors = Collections.singletonList(AttributesImporterUtils.createError(properties, "ImporterException", e.getMessage()));
                partImporterResult = new PartImporterResult(file, new ArrayList<>(), errors, null, null, null);
            }
        } else {
            List<String> errors = getNoImporterAvailableError(properties);
            partImporterResult = new PartImporterResult(file, new ArrayList<>(), errors, null, null, null);
        }

        // todo replace null values
        return new ImportPreview(null, null);
    }

    private Locale getUserLocale(String workspaceId) {
        Locale locale;
        try {
            User user = userManager.whoAmI(workspaceId);
            locale = new Locale(user.getLanguage());
        } catch (ApplicationException e) {
            LOGGER.log(Level.SEVERE, "Cannot fetch account info", e);
            locale = Locale.getDefault();
        }
        return locale;
    }

    private PartImporter selectPartImporter(File file) {
        PartImporter selectedImporter = null;
        for (PartImporter importer : partImporters) {
            if (importer.canImportFile(file.getName())) {
                selectedImporter = importer;
                break;
            }
        }
        return selectedImporter;
    }

    private PathDataImporter selectPathDataImporter(File file) {
        PathDataImporter selectedImporter = null;
        for (PathDataImporter importer : pathDataImporters) {
            if (importer.canImportFile(file.getName())) {
                selectedImporter = importer;
                break;
            }
        }
        return selectedImporter;
    }

    private List<String> getNoImporterAvailableError(Properties properties) {
        return Collections.singletonList(properties.getProperty("NoImporterAvailable"));
    }


    private boolean canChangePart(String workspaceId, PartRevision lastRevision, boolean autoCheckout)
            throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        return (autoCheckout && !lastRevision.isCheckedOut()) || (lastRevision.isCheckedOut() && lastRevision.getCheckOutUser().equals(user));
    }

    private ImportResult doPartImport(Properties properties, String workspaceId, String revisionNote, boolean autoCheckout, boolean autoCheckin,
                                      boolean permissiveUpdate, PartImporterResult partImporterResult) {

        List<String> errors = partImporterResult.getErrors();
        List<String> warnings = partImporterResult.getWarnings();
        Map<String, PartToImport> partsToImport = partImporterResult.getPartsToImport();
        List<PartToImport> listParts = new ArrayList<>();

        for (PartToImport part : partsToImport.values()) {

            try {
                PartMaster currentPartMaster = productManager.getPartMaster(new PartMasterKey(workspaceId, part.getNumber()));

                PartIteration partIteration = currentPartMaster.getLastRevision().getLastIteration();

                boolean hasAccess = productManager.canWrite(currentPartMaster.getLastRevision().getKey());

                if (part.hasAttributes() && (hasAccess && canChangePart(workspaceId, partIteration.getPartRevision(), autoCheckout))) {

                    //info : we create 2 instanceAttribute Lists to ensure separation between current list and updated list
                    List<InstanceAttribute> updatedInstanceAttributes = AttributesImporterUtils.getInstanceAttributes(properties, partIteration.getInstanceAttributes(), errors);//we will update data here
                    List<InstanceAttribute> currentInstanceAttributes = new ArrayList<>(updatedInstanceAttributes);//we will delete updated attributes from here

                    List<Attribute> attributes = part.getAttributes();
                    part.getNumber();
                    AttributesImporterUtils.updateAndCreateInstanceAttributes(lovManager, properties, attributes, currentInstanceAttributes, part.getNumber(), errors, workspaceId, updatedInstanceAttributes);
                    part.setInstanceAttributes(updatedInstanceAttributes);
                    if (revisionNote != null && !revisionNote.isEmpty()) {
                        part.setRevisionNote(revisionNote);
                    }
                    part.setPartIteration(partIteration);
                    listParts.add(part);

                } else if (permissiveUpdate && !hasAccess) {
                    warnings.add(AttributesImporterUtils.createError(properties, "NotAccess", part.getNumber()));
                    LOGGER.log(Level.WARNING, "No right on [" + part.getNumber() + "]");

                } else if (!canChangePart(workspaceId, partIteration.getPartRevision(), autoCheckout)) {
                    User user = userManager.checkWorkspaceReadAccess(workspaceId);

                    if (partIteration.getPartRevision().isCheckedOut() && !partIteration.getPartRevision().getCheckOutUser().equals(user)) {
                        String errorMessage = AttributesImporterUtils.createError(properties, "AlreadyCheckedOut", part.getNumber(), partIteration.getPartRevision().getCheckOutUser().getName());
                        if (permissiveUpdate) {
                            warnings.add(errorMessage);
                        } else {
                            errors.add(errorMessage);
                        }

                    } else if (!partIteration.getPartRevision().isCheckedOut()) {
                        String errorMessage = AttributesImporterUtils.createError(properties, "NotCheckedOut", part.getNumber());
                        if (permissiveUpdate) {
                            warnings.add(errorMessage);
                        } else {
                            errors.add(errorMessage);
                        }
                    }
                }

            } catch
                    (AccessRightException | UserNotFoundException | UserNotActiveException | WorkspaceNotFoundException
                            | PartMasterNotFoundException | PartRevisionNotFoundException | WorkspaceNotEnabledException e) {
                LOGGER.log(Level.WARNING, "Could not get PartMaster[" + part.getNumber() + "]", e);
                errors.add(e.getLocalizedMessage());
            }
        }

        if (!errors.isEmpty()) {
            return new ImportResult(partImporterResult.getImportedFile(), warnings, errors);
        }

        try {
            bulkPartUpdate(listParts, workspaceId, autoCheckout, autoCheckin, permissiveUpdate, errors, warnings);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, null, e);
            errors.add("Unhandled exception");
        }
        return new ImportResult(partImporterResult.getImportedFile(), warnings, errors);
    }

    public void bulkPartUpdate(List<PartToImport> parts, String workspaceId, boolean autoCheckout, boolean autoCheckin, boolean permissive, List<String> errors, List<String> warnings) throws Exception {

        LOGGER.log(Level.INFO, "Bulk parts update");
        User user = userManager.checkWorkspaceReadAccess(workspaceId);

        boolean errorOccured = false;
        Exception exception = null;

        for (PartToImport part : parts) {
            PartIteration partIteration = part.getPartIteration();

            try {
                PartMaster currentPartMaster = productManager.getPartMaster(new PartMasterKey(workspaceId, part.getNumber()));

                boolean isAutoCheckedOutByImport = false; //to check if checkout for the update

                if (autoCheckout && !currentPartMaster.getLastRevision().isCheckedOut() && productManager.canWrite(currentPartMaster.getLastRevision().getKey())) {
                    PartRevision currentPartRevision = productManager.checkOutPart(new PartRevisionKey(workspaceId, part.getNumber(), currentPartMaster.getLastRevision().getVersion()));
                    isAutoCheckedOutByImport = true;
                    partIteration = currentPartRevision.getLastIteration();
                }

                //Check if not permissive or permissive and checked out
                if (partIteration.getPartRevision().isCheckedOut() && partIteration.getPartRevision().getCheckOutUser().equals(user)) {
                    //Do not lose previous saved revision note if no note specified during import
                    if (part.getRevisionNote() == null) {
                        part.setRevisionNote(partIteration.getIterationNote());
                    }
                    productManager.updatePartIteration(partIteration.getKey(), part.getRevisionNote(), partIteration.getSource(), null, part.getInstanceAttributes(), null, null, null, null);
                } else {
                    throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException25", partIteration.getPartRevision().toString());
                }

                //CheckIn if checkout before
                if (autoCheckin && isAutoCheckedOutByImport && partIteration.getPartRevision().getCheckOutUser().equals(user)) {
                    try {
                        productManager.checkInPart(new PartRevisionKey(currentPartMaster.getKey(), currentPartMaster.getLastRevision().getVersion()));
                    } catch (NotAllowedException e) {
                        LOGGER.log(Level.WARNING, null, e);
                        warnings.add(e.getLocalizedMessage());
                    }
                }
            } catch (CreationException | PartMasterNotFoundException | EntityConstraintException | UserNotFoundException | WorkspaceNotFoundException | UserNotActiveException | PartUsageLinkNotFoundException | PartRevisionNotFoundException | AccessRightException | FileAlreadyExistsException e) {
                LOGGER.log(Level.WARNING, null, e);
                errors.add(e.getLocalizedMessage() + ": " + partIteration.getNumber());
                errorOccured = true;
                exception = e;

            } catch (NotAllowedException e) {
                LOGGER.log(Level.WARNING, null, e);
                if (permissive) {
                    warnings.add(e.getLocalizedMessage());
                } else {
                    errors.add(e.getLocalizedMessage());
                    errorOccured = true;
                    exception = e;
                }
            }
        }

        LOGGER.log(Level.INFO, "Bulk parts update finished");

        if (errorOccured) {
            throw exception;
        }
    }

}
