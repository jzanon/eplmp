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

package org.polarsys.eplmp.server.rest.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@ApiModel(value = "OAuthProviderPublicDTO", description = "This class is the representation of an {@link org.polarsys.eplmp.core.common.OAuthProvider} entity")
public class OAuthProviderPublicDTO implements Serializable {

    @ApiModelProperty(value = "Id of the auth provider")
    private Integer id;

    @ApiModelProperty(value = "Name of the auth provider")
    private String name;

    @ApiModelProperty(value = "Enabled state of the auth provider")
    private boolean enabled;

    @ApiModelProperty(value = "Base url of the auth provider")
    private String issuer;

    @ApiModelProperty(value = "Provider client ID")
    private String clientID;

    @ApiModelProperty(value = "Provider jws algorithm")
    private String jwsAlgorithm;

    @ApiModelProperty(value = "Redirect uri")
    private String redirectUri;

    public OAuthProviderPublicDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getJwsAlgorithm() {
        return jwsAlgorithm;
    }

    public void setJwsAlgorithm(String jwsAlgorithm) {
        this.jwsAlgorithm = jwsAlgorithm;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
}
