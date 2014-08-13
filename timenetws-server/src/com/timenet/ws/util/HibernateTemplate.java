package com.timenet.ws.util;
/**
 * auto GeneratedValue
 * @author Veeranna
 */
import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;



public class HibernateTemplate {

	public static Object save(Object obj){
		Transaction tx=null;
		Object ob=null;
		Session session=null;
		try
		{
			SessionFactory sf=HibernateUtil.getSessionFactory();
			 session=sf.openSession();
			tx=session.beginTransaction();
			ob=session.save(obj);
			tx.commit();
			
			LogFile.info("## Save the data successfully## ");

		}catch(Exception e){
			if(tx!=null)
				tx.rollback();
			LogFile.info("## HibernateException for saving the data: ## "+e.getMessage());
			e.printStackTrace();

		}finally{
			if(session!=null)
				session.close();
			}
		
		return ob;
	}

	public static Object load(Class cls,Serializable id){
		Transaction tx=null;
		Object ob=null;
		Session session=null;
		try
		{
			SessionFactory sf=HibernateUtil.getSessionFactory();
			session=sf.openSession();
			tx=session.beginTransaction();
			ob=session.load(cls,id);
			tx.commit();
			//session.close();
			LogFile.info("## Load the data successfully## ");

		}catch(Exception e){
			if(tx!=null)
				tx.rollback();
			LogFile.info("## HibernateException for loding the data: ## "+e.getMessage());
			e.printStackTrace();

		}finally{
			if(session!=null)
				session.close();
			}
		return ob;
	}


	public static void delete(Class cls,Serializable id){
		Transaction tx=null;
		Object ob=null;
		Session session=null;
		try
		{
			SessionFactory sf=HibernateUtil.getSessionFactory();
			session=sf.openSession();
			tx=session.beginTransaction();
			ob=session.load(cls,id);
			session.delete(ob);
			tx.commit();
			//session.close();
			LogFile.info("## Delete the data successfully## ");

		}catch(Exception e){
			if(tx!=null)
				tx.rollback();
			LogFile.info("## HibernateException for deleting the data: ## "+e.getMessage());
			e.printStackTrace();

		}finally{
			if(session!=null)
			session.close();
		}

	}


	public static void update(Object obj){
		Transaction tx=null;
		Session session=null;
		try
		{
			SessionFactory sf=HibernateUtil.getSessionFactory();
			session=sf.openSession();
			tx=session.beginTransaction();
			session.update(obj);
			tx.commit();
			//session.close();
			LogFile.info("## Update the data successfully## ");

		}catch(Exception e){
			if(tx!=null)
				tx.rollback();
			LogFile.info("## HibernateException for update the data: ## "+e.getMessage());
			e.printStackTrace();

		}finally{
			if(session!=null)
				session.close();
			}

	}


	public static List findList(String hql,Object ...args){
		Transaction tx=null;
		List list=null;
		Session session=null;
		try
		{
			SessionFactory sf=HibernateUtil.getSessionFactory();
			session=sf.openSession();
			tx=session.beginTransaction();

			Query q=session.createQuery(hql);
		if(args!=null){
			
			for(int i=0;i<args.length;i++)
				q=q.setParameter(i, args[i]);
		}
			list=q.list();

			tx.commit();
			//session.close();
			LogFile.info("## Find the data successfully## ");

		}catch(Exception e){
			if(tx!=null)
				tx.rollback();
			LogFile.info("## HibernateException for Finding the data: ## "+e.getMessage());
			e.printStackTrace();

		}finally{
			if(session!=null)
				session.close();
			}
		return list;
	}

	public static List findList(String hql,int start,int total,Object ...args){
		Transaction tx=null;
		List list=null;
		Session session=null;
		try
		{
			SessionFactory sf=HibernateUtil.getSessionFactory();
		    session=sf.openSession();
			tx=session.beginTransaction();

			Query q=session.createQuery(hql);
			q=q.setFirstResult(start);
			q=q.setMaxResults(total);

			for(int i=0;i<args.length;i++)
				q=q.setParameter(i, args[i]);
			list=q.list();

			tx.commit();
			//session.close();
			LogFile.info("## Find the data successfully## ");

		}catch(Exception e){
			if(tx!=null)
				tx.rollback();
			LogFile.info("## HibernateException for finding the data: ## "+e.getMessage());
			e.printStackTrace();

		}finally{
			if(session!=null)
				session.close();
			}
		return list;
	}


	public static Object findObject(String hql,Object ...args){
		Transaction tx=null;
		Object obj=null;
		Session session=null;
		try
		{
			SessionFactory sf=HibernateUtil.getSessionFactory();
			 session=sf.openSession();
			tx=session.beginTransaction();

			Query q=session.createQuery(hql);

			for(int i=0;i<args.length;i++)
				q=q.setParameter(i, args[i]);
			if(q.list()!=null && q.list().size()>0)
			obj=q.list().get(0);

			tx.commit();
			//session.close();
			LogFile.info("## Find  the data successfully## ");

		}catch(Exception e){
			if(tx!=null)
				tx.rollback();
			LogFile.info("## HibernateException for finding the data: ## "+e.getMessage());
			e.printStackTrace();

		}
		finally{
			if(session!=null)
			session.close();
		}
		return obj;
	}
	
	public static Object findsqlObject(String sql,Object ...args){
		Transaction tx=null;
		Session session=null;
		Object obj=null;
		try
		{
			SessionFactory sf=HibernateUtil.getSessionFactory();
			session=sf.openSession();
			tx=session.beginTransaction();

			 SQLQuery q = session.createSQLQuery(sql);

				for(int i=0;i<args.length;i++)
					q=(SQLQuery) q.setParameter(i, args[i]);
				if(q.list()!=null && q.list().size()>0)
				obj=q.list().get(0);

			tx.commit();
			//session.close();
			LogFile.info("## Find the data successfully## ");

		}catch(Exception e){
			if(tx!=null)
				tx.rollback();
			LogFile.info("## HibernateException for Finding the data: ## "+e.getMessage());
			e.printStackTrace();

		}finally{
			if(session!=null)
				session.close();
			}
		return obj;
	}
	
	
	public static List findsqlList(String sql,Object ...args){
		Transaction tx=null;
		List list=null;
		Session session=null;
		try
		{
			SessionFactory sf=HibernateUtil.getSessionFactory();
			session=sf.openSession();
			tx=session.beginTransaction();

			 SQLQuery q = session.createSQLQuery(sql);
		if(args!=null){
			
			for(int i=0;i<args.length;i++)
				q.setParameter(i, args[i]);
			 
		}
			list=q.list();

			tx.commit();
			//session.close();
			LogFile.info("## Find the data successfully## ");

		}catch(Exception e){
			if(tx!=null)
				tx.rollback();
			LogFile.info("## HibernateException for Finding the data: ## "+e.getMessage());
			e.printStackTrace();

		}finally{
			if(session!=null)
				session.close();
			}
		return list;
	}
	
	public static List findsqlpageList(String sql, String category, int pageNumber, int pageSize){
		Transaction tx=null;
		List list=null;
		Session session=null;
		try
		{
			SessionFactory sf=HibernateUtil.getSessionFactory();
			session=sf.openSession();
			tx=session.beginTransaction();
			
			 SQLQuery q = session.createSQLQuery(sql);
			 q.setParameter(0, category);
		     q.setFirstResult(pageSize * (pageNumber - 1));
		     q.setMaxResults(pageSize);
		     list = q.list();
		     
			 tx.commit();
			//session.close();
			LogFile.info("## Find the data successfully## ");

		}catch(Exception e){
			if(tx!=null)
				tx.rollback();
			LogFile.info("## HibernateException for Finding the data: ## "+e.getMessage());
			e.printStackTrace();

		}finally{
			if(session!=null)
				session.close();
			}
		return list;
	}

	
	public static List findsqlpageoffsetList(String sql, String category, int pageNumber, int pageSize){
		Transaction tx=null;
		List list=null;
		Session session=null;
		try
		{
			SessionFactory sf=HibernateUtil.getSessionFactory();
			session=sf.openSession();
			tx=session.beginTransaction();
			
			
			 int offset = (pageNumber - 1) * 10;
			 SQLQuery q = session.createSQLQuery(sql + offset);
			 q.setParameter(0, category);
			 
			
			 
			// q + limit offset ,10;
		     q.setFirstResult(pageSize * (pageNumber - 1));
		     q.setMaxResults(pageSize);
		     list = q.list();
		     
			 tx.commit();
			//session.close();
			LogFile.info("## Find the data successfully## ");

		}catch(Exception e){
			if(tx!=null)
				tx.rollback();
			LogFile.info("## HibernateException for Finding the data: ## "+e.getMessage());
			e.printStackTrace();

		}finally{
			if(session!=null)
				session.close();
			}
		return list;
	}


}
