package com.zimbra.cs.account.ldap;

import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.EmailUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Alias;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Identity;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.Zimlet;
import com.zimbra.cs.util.Zimbra;

public class LdapDIT {
    /*
     * This is our default ldap DIT.  All DNs/RDNs location is hardcoded to avoid 
     * mis-configuration.  
     * 
     * To customize the DIT to a different layout, set the zimbra_class_provisioning
     * localconfig key to com.zimbra.cs.account.ldap.custom.CustomLdapProvisioning, 
     * which will use the CustomLdapDIT class that can be customized by a set of
     * localconfig keys.
     * 
     */
    
    /*
     * Vatiable Naming Conventions:
     * 
     *              RDN : {attr-name}={attr-value}
     *               DN : List of comma (,) seperated RDNs
     *          DEFAULT : Means the variable has a hardcoded value, which can be referred to 
     *                    from subclasses, but cannot be changed.  If a subclass need to use 
     *                    different values it has to define it's own variables.
     * 
     *         BASE_RDN : A relative RDN under that entries of the same kind reside.
     * DEFAULT_BASE_RDN : A hardcoded BASE_RDN that cannot be changed in subclasses.
     *  NAMING_RDN_ATTR : The attribute for the left-most RDN of an entry. each entry type must have a NAMING_RDN_ATTR.
     *          BASE_DN : An absolute DN under that a left-most RDN resides.
     */
    
    /*
     * Defaults tht can be used in subclasses but cannot be changed in subclasses.
     * If a subclass need to use different values it has to define it's own variables.
     */
    protected final String DEFAULT_CONFIG_BASE_DN        = "cn=zimbra";
    
    protected final String DEFAULT_BASE_RDN_ADMIN        = "cn=admins";
    protected final String DEFAULT_BASE_RDN_ACCOUNT      = "ou=people";
    protected final String DEFAULT_BASE_RDN_COS          = "cn=cos";
    protected final String DEFAULT_BASE_RDN_MIME         = "cn=mime";
    protected final String DEFAULT_BASE_RDN_SERVER       = "cn=servers";
    protected final String DEFAULT_BASE_RDN_ZIMLET       = "cn=zimlets";
    
    protected final String DEFAULT_NAMING_RDN_ATTR_ACCOUNT       = "uid";
    protected final String DEFAULT_NAMING_RDN_ATTR_COS           = "cn";
    protected final String DEFAULT_NAMING_RDN_ATTR_GLOBALCONFIG  = "cn";
    protected final String DEFAULT_NAMING_RDN_ATTR_MIME          = "cn";
    protected final String DEFAULT_NAMING_RDN_ATTR_SERVER        = "cn";
    protected final String DEFAULT_NAMING_RDN_ATTR_ZIMLET        = "cn";
    
    /*
     * Variables that has to be set in the init method
     */
    protected String BASE_DN_CONFIG_BRANCH;
    protected String BASE_RDN_ACCOUNT;
    
    protected String BASE_DN_ADMIN;
    protected String BASE_DN_ACCOUNT;
    protected String BASE_DN_COS; 
    protected String BASE_DN_MIME;
    protected String BASE_DN_SERVER;
    protected String BASE_DN_ZIMLET;
     
    protected String NAMING_RDN_ATTR_ACCOUNT;
    protected String NAMING_RDN_ATTR_COS;
    protected String NAMING_RDN_ATTR_GLOBALCONFIG;
    protected String NAMING_RDN_ATTR_MIME;
    protected String NAMING_RDN_ATTR_SERVER;
    protected String NAMING_RDN_ATTR_ZIMLET;

    protected String DN_GLOBALCONFIG;


    public LdapDIT() {
        init();
        verify();
    }
    
    protected void init() {
        BASE_DN_CONFIG_BRANCH = DEFAULT_CONFIG_BASE_DN;

        BASE_RDN_ACCOUNT  = DEFAULT_BASE_RDN_ACCOUNT;

        NAMING_RDN_ATTR_ACCOUNT       = DEFAULT_NAMING_RDN_ATTR_ACCOUNT;
        NAMING_RDN_ATTR_COS           = DEFAULT_NAMING_RDN_ATTR_COS;
        NAMING_RDN_ATTR_GLOBALCONFIG  = DEFAULT_NAMING_RDN_ATTR_GLOBALCONFIG;
        NAMING_RDN_ATTR_MIME          = DEFAULT_NAMING_RDN_ATTR_MIME;
        NAMING_RDN_ATTR_SERVER        = DEFAULT_NAMING_RDN_ATTR_SERVER;
        NAMING_RDN_ATTR_ZIMLET        = DEFAULT_NAMING_RDN_ATTR_ZIMLET;
        
        DN_GLOBALCONFIG      = NAMING_RDN_ATTR_GLOBALCONFIG + "=config" + "," + BASE_DN_CONFIG_BRANCH; 
       
        BASE_DN_ADMIN        = DEFAULT_BASE_RDN_ADMIN       + "," + BASE_DN_CONFIG_BRANCH;
        BASE_DN_COS          = DEFAULT_BASE_RDN_COS         + "," + BASE_DN_CONFIG_BRANCH; 
        BASE_DN_MIME         = DEFAULT_BASE_RDN_MIME        + "," + DN_GLOBALCONFIG;
        BASE_DN_SERVER       = DEFAULT_BASE_RDN_SERVER      + "," + BASE_DN_CONFIG_BRANCH;
        BASE_DN_ZIMLET       = DEFAULT_BASE_RDN_ZIMLET      + "," + BASE_DN_CONFIG_BRANCH;
    }
    
    private final void verify() {
        if (BASE_DN_CONFIG_BRANCH == null ||
            BASE_RDN_ACCOUNT == null ||
            NAMING_RDN_ATTR_ACCOUNT == null ||
            NAMING_RDN_ATTR_COS == null ||
            NAMING_RDN_ATTR_GLOBALCONFIG == null ||
            NAMING_RDN_ATTR_MIME == null ||
            NAMING_RDN_ATTR_SERVER == null ||
            NAMING_RDN_ATTR_ZIMLET == null ||
            BASE_DN_ADMIN == null ||
            BASE_DN_COS == null ||
            BASE_DN_MIME == null ||
            BASE_DN_SERVER == null ||
            BASE_DN_ZIMLET == null ||
            DN_GLOBALCONFIG == null)
            Zimbra.halt("Unable to initialize LDAP DIT");
    }
    
    /*
     * config branch
     */
    public String configBranchBaseDn() {
        return BASE_DN_CONFIG_BRANCH;
    }
    
    /*
     * ===========
     *   account
     * ===========
     */
    public String accountNamingRdnAttr() {
        return NAMING_RDN_ATTR_ACCOUNT;
    }

    // TODO deprecate and fix all call points that still use it
    public String emailToDN(String localPart, String domain) {
        return NAMING_RDN_ATTR_ACCOUNT + "=" + LdapUtil.escapeRDNValue(localPart) + "," + domainToAccountBaseDN(domain);
    }
    
    // TODO deprecate and fix all call points that still use it
    public String emailToDN(String email) {
        String[] parts = EmailUtil.getLocalPartAndDomain(email);
        return emailToDN(parts[0], parts[1]);
    }
    
    public String accountDN(String baseDn, Attributes attrs, String domain) throws ServiceException, NamingException {
        if (baseDn == null)
            baseDn = domainToAccountBaseDN(domain);
        
        String rdnAttr = NAMING_RDN_ATTR_ACCOUNT;
        
        String rdnValue = LdapUtil.getAttrString(attrs, rdnAttr);
        
        if (rdnValue == null)
            throw ServiceException.FAILURE("missing rdn attribute" + rdnAttr, null);

        return rdnAttr + "=" + LdapUtil.escapeRDNValue(rdnValue) + "," + baseDn;
    }
    
    /**
     * Given a dn like "uid=foo,ou=people,dc=widgets,dc=com", return the string "foo@widgets.com".
     * 
     * @param dn
     * @return
     */
    public String dnToEmail(String dn) throws ServiceException {
        String [] parts = dn.split(",");
        StringBuffer domain = new StringBuffer(dn.length());
        
        String namingAttr = accountNamingRdnAttr() + "=";
        String namingAttrValue = null;
        
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].startsWith("dc=")) {
                if (domain.length() > 0)
                    domain.append(".");
                domain.append(LdapUtil.unescapeRDNValue(parts[i].substring(3)));
            } else if (i==0 && parts[i].startsWith(namingAttr)) {
                namingAttrValue = LdapUtil.unescapeRDNValue(parts[i].substring(namingAttr.length()));
            }
        }
        if (namingAttrValue == null)
            throw ServiceException.FAILURE("unable to map dn [" + dn + "] to email", null);
        if (domain.length() == 0)
            return namingAttrValue;
        return new StringBuffer(namingAttrValue).append('@').append(domain).toString();
    }
    
   
    /*
     * =================
     *   admin account
     * =================
     */
    public String adminBaseDN() {
        return BASE_DN_ADMIN;
    }
    
    public String adminNameToDN(String name) {
        return NAMING_RDN_ATTR_ACCOUNT + "=" + LdapUtil.escapeRDNValue(name) + "," + BASE_DN_ADMIN;
    }
  
    
    /*
     * ==========
     *   alias
     * ==========
     */
    public String aliasDN(String targetEntryDn, String aliasLocalPart, String aliasDomain) {
        return emailToDN(aliasLocalPart, aliasDomain);
    }
   
    
    /*
     * =======
     *   COS
     * =======
     */
    public String cosBaseDN() {
        return BASE_DN_COS;
    }
    
    public String cosNametoDN(String name) {
        return NAMING_RDN_ATTR_COS + "=" + LdapUtil.escapeRDNValue(name) + "," + BASE_DN_COS;
    }
    
    /*
     * =====================
     *   distribution list 
     * =====================
     */
    
    /*
     * ==========
     *   domain
     * ==========
     */
    // TODO deprecate and fix all call points that still use it
    public String domainToAccountBaseDN(String domain) {
        if (BASE_RDN_ACCOUNT.length()==0)
            return LdapUtil.domainToDN(domain);
        else
            return BASE_RDN_ACCOUNT + "," + LdapUtil.domainToDN(domain);
    }

    // TODO deprecate and fix all call points that still use it
    public String domainToAccountBaseDN(LdapDomain domain) {
        if (BASE_RDN_ACCOUNT.length()==0)
            return domain.getDN();
        else
            return BASE_RDN_ACCOUNT + "," + domain.getDN();
    }

    // TODO deprecate and fix all call points that still use it
    public String domainDNToAccountBaseDN(String domainDN) {
        if (BASE_RDN_ACCOUNT.length()==0)
            return domainDN;
        else
            return BASE_RDN_ACCOUNT + "," + domainDN;
    }
    
    
    /*
     * ==============
     *   globalconfig
     * ==============
     */
    public String configDN() {
        return DN_GLOBALCONFIG;
    }

   
    /*
     * ========
     *   mime
     * ========
     */
    public String mimeConfigToDN(String name) {
        name = LdapUtil.escapeRDNValue(name);
        return NAMING_RDN_ATTR_MIME + "=" + name + "," + BASE_DN_MIME;
    }
    
    
    /*
     * ==========
     *   server
     * ==========
     */
    public String serverBaseDN() {
        return BASE_DN_SERVER;
    }
    
    public String serverNametoDN(String name) {
        return NAMING_RDN_ATTR_SERVER + "=" + LdapUtil.escapeRDNValue(name) + "," + BASE_DN_SERVER;
    }
    
    
    /*
     * ==========
     *   zimlet
     * ==========
     */    
    public String zimletNameToDN(String name) {
        return NAMING_RDN_ATTR_ZIMLET + "=" + LdapUtil.escapeRDNValue(name) + "," + BASE_DN_ZIMLET;
    }
    
    
    /*
     * ========================================================================================
     */
    protected SpecialAttrs handleSpecialAttrs(Map<String, Object> attrs) throws ServiceException {
        SpecialAttrs specialAttrs = new SpecialAttrs();
        if (attrs != null) {
            specialAttrs.handleZimbraId(attrs);
            
            // default is don't support pseudo attrs
            // if the pseudo attr is present and not handled here, a NamingExeption will be thrown 
            // when the entry is being created

        }
        return specialAttrs;
    }
    
    public String getNamingRdnAttr(Entry entry) throws ServiceException {
        if (entry instanceof Account ||
            entry instanceof DistributionList ||
            entry instanceof Alias)
            return NAMING_RDN_ATTR_ACCOUNT;
        else if (entry instanceof Cos)
            return NAMING_RDN_ATTR_COS;
        else if (entry instanceof Config) 
            return NAMING_RDN_ATTR_GLOBALCONFIG;
        else if (entry instanceof DataSource)
            return Provisioning.A_zimbraDataSourceName;
        else if (entry instanceof Domain)   
            return Provisioning.A_dc;
        else if (entry instanceof Identity)
            return Provisioning.A_zimbraPrefIdentityName;   
        else if (entry instanceof Server)
            return NAMING_RDN_ATTR_SERVER;
        else if (entry instanceof Zimlet)
            return NAMING_RDN_ATTR_ZIMLET;
        else
            throw ServiceException.FAILURE("entry type " + entry.getClass().getCanonicalName() + " is not supported by getNamingRdnAttr", null);
    }

}
