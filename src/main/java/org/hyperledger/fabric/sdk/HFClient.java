/*
 *  Copyright 2016, 2017 DTCC, Fujitsu Australia Software Technology, IBM - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 	  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.hyperledger.fabric.sdk;


import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.util.internal.StringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

public class HFClient {

    private CryptoSuite cryptoSuite;

    static {

        if (null == System.getProperty("org.hyperledger.fabric.sdk.logGRPC")) {
            // Turn this off by default!
            Logger.getLogger("io.netty").setLevel(Level.OFF);
            Logger.getLogger("io.grpc").setLevel(Level.OFF);

        }
    }

    private static final Log logger = LogFactory.getLog(HFClient.class);

    private final Map<String, Chain> chains = new HashMap<>();


    public User getUserContext() {
        return userContext;
    }

    private User userContext;

    // The member services used for this chain
    private MemberServices memberServices;

    private HFClient() {

    }

    public CryptoSuite getCryptoSuite() {
        return cryptoSuite;
    }

    public void setCryptoSuite(CryptoSuite cryptoSuite) throws CryptoException, InvalidArgumentException {
        this.cryptoSuite = cryptoSuite;
        this.cryptoSuite.init();
    }

    /**
     * createNewInstance create a new instance of the HFClient
     *
     * @return client
     */
    public static HFClient createNewInstance() {
        return new HFClient();
    }

    /**
     * newChain - already configured chain.
     *
     * @param name
     * @return
     * @throws InvalidArgumentException
     */

    public Chain newChain(String name) throws InvalidArgumentException {
        logger.trace("Creating chain :" + name);
        Chain newChain = Chain.createNewInstance(name, this);
        chains.put(name, newChain);
        return newChain;
    }

    /**
     * Create a new chain
     *
     * @param name               The chains name
     * @param orderer            Order to create the chain with.
     * @param chainConfiguration Chain configuration data.
     * @return
     * @throws TransactionException
     * @throws InvalidArgumentException
     */

    public Chain newChain(String name, Orderer orderer, ChainConfiguration chainConfiguration) throws TransactionException, InvalidArgumentException {

        logger.trace("Creating chain :" + name);
        Chain newChain = Chain.createNewInstance(name, this, orderer, chainConfiguration);
        chains.put(name, newChain);
        return newChain;
    }

    /**
     * newPeer create a new peer
     *
     * @param name
     * @param grpcURL to the peer's location
     * @param properties
     *
     *<p>
     * Supported properties
     * <ul>
     * <li>pemFile - File location for x509 pem certificate for SSL.</li>
     * <li>trustServerCertificate - boolen(true/false) override CN to match pemFile certificate -- for development only.
     * If the pemFile has the target server's certificate (instead of a CA Root certificate),
     * instruct the TLS client to trust the CN value of the certificate in the pemFile,
     * useful in development to get past default server hostname verification during
     * TLS handshake, when the server host name does not match the certificate.
     * </li>
     * <li>hostnameOverride - Specify the certificates CN -- for development only.</li>
     * </ul>
     *
     * @return Peer
     * @throws InvalidArgumentException
     */

    public Peer newPeer(String name, String grpcURL, Properties properties) throws InvalidArgumentException {
        return Peer.createNewInstance(name, grpcURL, properties);
    }

    /**
     * newPeer create a new peer
     *
     * @param name
     * @param grpcURL to the peer's location
     * @return Peer
     * @throws InvalidArgumentException
     */

    public Peer newPeer(String name, String grpcURL) throws InvalidArgumentException {
        return Peer.createNewInstance(name, grpcURL, null);
    }


    /**
     * Get the member service associated this chain.
     *
     * @return MemberServices associated with the chain, or undefined if not set.
     */
    public MemberServices getMemberServices() {
        return this.memberServices;
    }

    /**
     * Set the member service
     *
     * @param memberServices The MemberServices instance
     * @throws CryptoException
     */
    public void setMemberServices(MemberServices memberServices) {
        this.memberServices = memberServices;
        this.memberServices.setCryptoSuite(this.cryptoSuite);
    }


    /**
     * getChain by name
     *
     * @param name
     * @return
     */

    public Chain getChain(String name) {
        return chains.get(name);
    }

    /**
     * newInstallProposalRequest get new Install proposal request.
     *
     * @return InstallProposalRequest
     */
    public InstallProposalRequest newInstallProposalRequest() {
        return new InstallProposalRequest();
    }

    /**
     * newInstantiationProposalRequest get new instantiation proposal request.
     *
     * @return InstantiateProposalRequest
     */

    public InstantiateProposalRequest newInstantiationProposalRequest() {
        return new InstantiateProposalRequest();
    }

    /**
     * newInvokeProposalRequest  get new invoke proposal request.
     *
     * @return InvokeProposalRequest
     */

    public InvokeProposalRequest newInvokeProposalRequest() {
        return InvokeProposalRequest.newInstance();
    }

    /**
     * newQueryProposalRequest get new query proposal request.
     *
     * @return QueryProposalRequest
     */

    public QueryProposalRequest newQueryProposalRequest() {
        return QueryProposalRequest.newInstance();
    }

    /**
     * Set the User context for this client.
     *
     * @param userContext
     */

    public void setUserContext(User userContext) throws InvalidArgumentException {


        if (userContext == null) {
            throw new InvalidArgumentException("setUserContext is null");
        }
        Enrollment enrollment = userContext.getEnrollment();
        if (enrollment == null) {
            throw new InvalidArgumentException("setUserContext has no Enrollment set");
        }

        if (StringUtil.isNullOrEmpty(userContext.getMSPID())) {
            throw new InvalidArgumentException("setUserContext user's MSPID is missing");
        }

        if (StringUtil.isNullOrEmpty(userContext.getName())) {
            throw new InvalidArgumentException("setUserContext user's name is missing");
        }

        if (StringUtil.isNullOrEmpty(enrollment.getCert())) {
            throw new InvalidArgumentException("setUserContext Enrollment missing user certificate.");
        }
        if (null == enrollment.getKey()) {
            throw new InvalidArgumentException("setUserContext has no Enrollment missing signing key");
        }
        if (StringUtil.isNullOrEmpty(enrollment.getPublicKey())) {
            throw new InvalidArgumentException("setUserContext Enrollment missing user public key.");
        }

        this.userContext = userContext;
    }

    /**
     * Create a new Eventhub.
     *
     * @param name name of Orderer.
     * @param grpcURL  url location of orderer grpc or grpcs protocol.
     * @param properties
     *<p>
     * Supported properties
     * <ul>
     * <li>pemFile - File location for x509 pem certificate for SSL.</li>
     * <li>trustServerCertificate - boolen(true/false) override CN to match pemFile certificate -- for development only.
     * If the pemFile has the target server's certificate (instead of a CA Root certificate),
     * instruct the TLS client to trust the CN value of the certificate in the pemFile,
     * useful in development to get past default server hostname verification during
     * TLS handshake, when the server host name does not match the certificate.
     * </li>
     * <li>hostnameOverride - Specify the certificates CN -- for development only.</li>
     * </ul>
     * @return The orderer.
     * @throws InvalidArgumentException
     */

    public EventHub newEventHub(String name, String grpcURL, Properties properties) throws InvalidArgumentException {
        return EventHub.createNewInstance(name, grpcURL, properties);
    }


    /**
     * Create a new event hub
     *
     * @param name Name of eventhup should match peer's name it's associated with.
     * @param grpcURL The http url location of the event hub
     * @return event hub
     */

    public EventHub newEventHub(String name, String grpcURL) throws InvalidArgumentException {
        return newEventHub(name, grpcURL, null);
    }

    /**
     * Create a new urlOrderer.
     * @param name name of the orderer.
     * @param grpcURL url location of orderer grpc or grpcs protocol.
     * @return a new Orderer.
     * @throws InvalidArgumentException
     */

    public Orderer newOrderer(String name, String grpcURL) throws InvalidArgumentException {
        return newOrderer(name, grpcURL, null);
    }

    /**
     * Create a new orderer.
     *
     * @param name name of Orderer.
     * @param grpcURL  url location of orderer grpc or grpcs protocol.
     * @param properties
     *<p>
     * Supported properties
     * <ul>
     * <li>pemFile - File location for x509 pem certificate for SSL.</li>
     * <li>trustServerCertificate - boolen(true/false) override CN to match pemFile certificate -- for development only.
     * If the pemFile has the target server's certificate (instead of a CA Root certificate),
     * instruct the TLS client to trust the CN value of the certificate in the pemFile,
     * useful in development to get past default server hostname verification during
     * TLS handshake, when the server host name does not match the certificate.
     * </li>
     * <li>hostnameOverride - Specify the certificates CN -- for development only.</li>
     * </ul>
     * @return The orderer.
     * @throws InvalidArgumentException
     */

    public Orderer newOrderer(String name, String grpcURL, Properties properties) throws InvalidArgumentException {
        return Orderer.createNewInstance(name, grpcURL, properties);
    }
}
