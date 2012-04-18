package com.zyeeda.framework.ldap;

import javax.naming.ldap.Control;

/*
 * By default, LDAP compliant directories forbid the deletion of entries with children. 
 * It is only permitted to delete leaf entries. However the Tree Delete Control provided
 * by IBM Tivoli Directory Server 5.2/6.0 extends the delete operation and allows the removal
 * of sub trees within a directory using a single delete request. 
 * In addition to Tivoli Directory Server this control is also supported by Microsoft Active Directory.
 * When using JNDI methods to delete an entry within a directory, it is also not possible to remove whole subtrees at once.
 * This is an expected behaviour because the underlying JNDI service provider uses LDAP. 
 * It is also possible to utilize the described LDAP control within JNDI to overcome this limitation.
 * In order to demonstrate this, Listing 3 contains a Java class which implements the JNDI interface Control (see above).
 * The method getId provides the OID of the Tree Delete Control. isCritical signals that the client considers the support of the control as critical.
 * This property could be made configurable, but in this case a constant value is adequate. Since the definition of the control does not provide specific parameters, the method getEncodedValue returns null.
 */
public class TreeDeleteControl implements Control {

	private static final long serialVersionUID = 765855337893417383L;

	public String getID() {
        return "1.2.840.113556.1.4.805";
    }

    public boolean isCritical() {
        return Control.CRITICAL;
    }

    public byte[] getEncodedValue() {
        return null;
    }
}