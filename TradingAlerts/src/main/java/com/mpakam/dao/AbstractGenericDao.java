package com.mpakam.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.transaction.Transactional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@SuppressWarnings("unchecked")
@Repository
@Transactional
public abstract class AbstractGenericDao<E> implements GenericDao<E> {

	private final Class<E> entityClass;

	public AbstractGenericDao() {
		this.entityClass = (Class<E>) ((ParameterizedType) this.getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
	}
	
	@Autowired
	private SessionFactory sessionFactory;

	protected Session getSession() {
		return this.sessionFactory.getCurrentSession();
	}

	@Override
	public E findById(final Serializable id) {
		return (E) getSession().get(this.entityClass, id);
	}

	@Override
	public Serializable save(E entity) {
		return getSession().save(entity);
	}

	@Override
	public void saveOrUpdate(E entity) {
		getSession().saveOrUpdate(entity);
	}
	
	@Override
	@Transactional
	public void merge(E entity) {
		getSession().merge(entity);
	}

	@Override
	public void delete(E entity) {
		getSession().delete(entity);
	}

	@Override
	public void deleteAll() {
		List<E> entities = findAll();
		for (E entity : entities) {
			getSession().delete(entity);
		}
	}

	@Override
	public List<E> findAll() {
		return getSession().createCriteria(this.entityClass).list();
	}

	@Override
	public List<E> findAllByExample(E entity) {
		Example example = Example.create(entity).ignoreCase().enableLike().excludeZeroes();
		return getSession().createCriteria(this.entityClass).add(example).list();
	}

	@Override
	public void clear() {
		getSession().clear();

	}

	@Override
	public void flush() {
		getSession().flush();

	}
	 
}