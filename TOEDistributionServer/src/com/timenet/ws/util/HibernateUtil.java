package com.timenet.ws.util;


import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

/*******************************
* @author Veeranna Sulikeri
********************************/ 

public class HibernateUtil {
	private static final SessionFactory sessionFactory;
	static {
		try {
			// Create the SessionFactory from hibernate.cfg.xml
			sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();
		} catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			LogFile.error("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}