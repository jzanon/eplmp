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

package org.polarsys.eplmp.core.common;

import javax.json.JsonObject;
import javax.persistence.*;
import java.io.Serializable;

/**
 * The OAuthProvider class holds oauth providers settings
 *
 * @author Morgan Guimard
 */
@Table(name = "OAUTHPROVIDER")
@javax.persistence.Entity
@NamedQueries({
        @NamedQuery(name = "OAuthProvider.findAll", query = "SELECT o FROM OAuthProvider o")
})
public class OAuthProvider implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;

    private String name;
    private boolean enabled;

    private String issuer;
    private String clientID;
    private String jwsAlgorithm;
    private String jwkSetURL;
    private String redirectUri;
    // todo : encrypt
    private String secret;

    public OAuthProvider() {
    }

    public OAuthProvider(String name, boolean enabled, String issuer, String clientID, String jwsAlgorithm, String jwkSetURL, String redirectUri, String secret) {
        this.name = name;
        this.enabled = enabled;
        this.issuer = issuer;
        this.clientID = clientID;
        this.jwsAlgorithm = jwsAlgorithm;
        this.jwkSetURL = jwkSetURL;
        this.redirectUri = redirectUri;
        this.secret = secret;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getJwkSetURL() {
        return jwkSetURL;
    }

    public void setJwkSetURL(String jwkSetURL) {
        this.jwkSetURL = jwkSetURL;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
