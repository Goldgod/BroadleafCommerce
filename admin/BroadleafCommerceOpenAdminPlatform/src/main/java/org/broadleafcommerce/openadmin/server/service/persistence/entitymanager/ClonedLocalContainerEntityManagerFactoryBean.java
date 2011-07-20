package org.broadleafcommerce.openadmin.server.service.persistence.entitymanager;

import java.lang.reflect.Proxy;
import java.util.Properties;

import javax.persistence.spi.PersistenceUnitInfo;

import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.Jpa2PersistenceUnitInfoDecorator;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;

public class ClonedLocalContainerEntityManagerFactoryBean extends LocalContainerEntityManagerFactoryBean {
	
	protected String clonePersistenceUnitName;

	@Override
	protected PersistenceUnitInfo determinePersistenceUnitInfo(PersistenceUnitManager persistenceUnitManager) {
		PersistenceUnitInfo pui = persistenceUnitManager.obtainPersistenceUnitInfo(getClonePersistenceUnitName());
		MutablePersistenceUnitInfo temp;
        if (pui != null && Proxy.isProxyClass(pui.getClass())) {
			// JPA 2.0 PersistenceUnitInfo decorator with a SpringPersistenceUnitInfo as target
			Jpa2PersistenceUnitInfoDecorator dec = (Jpa2PersistenceUnitInfoDecorator) Proxy.getInvocationHandler(pui);
			temp = (MutablePersistenceUnitInfo) dec.getTarget();
		}
		else {
			// Must be a raw JPA 1.0 SpringPersistenceUnitInfo instance
			temp = (MutablePersistenceUnitInfo) pui;
		}
        temp.setJtaDataSource(null);
        temp.setNonJtaDataSource(getDataSource());
        if (temp.getProperties() != null) {
        	Properties props = temp.getProperties();
        	if (props != null) {
        		checkProps:{
	        		for (Object key : props.keySet()) {
	        			if (key.equals("hibernate.hbm2ddl.auto")) {
	        				temp.getProperties().setProperty((String) key, "create");
	        				break checkProps;
	        			}
	        		}
	        		temp.getProperties().setProperty("hibernate.hbm2ddl.auto", "create");
        		}
        	}
        }
        return pui;
	}

	public String getClonePersistenceUnitName() {
		return clonePersistenceUnitName;
	}

	public void setClonePersistenceUnitName(String clonePersistenceUnitName) {
		this.clonePersistenceUnitName = clonePersistenceUnitName;
	}
}
