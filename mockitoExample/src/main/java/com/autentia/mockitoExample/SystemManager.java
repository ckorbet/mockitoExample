package com.autentia.mockitoExample;

import java.util.ArrayList;
import java.util.Collection;

import javax.naming.OperationNotSupportedException;

import com.autentia.mockitoExample.dao.AuthDAO;
import com.autentia.mockitoExample.dao.GenericDAO;
import com.autentia.mockitoExample.dao.User;

public class SystemManager {

	private final AuthDAO authDao;
	private final GenericDAO dao;


	public SystemManager(final AuthDAO authDao, final GenericDAO dao) {
		super();
		this.authDao = authDao;
		this.dao = dao;
	}

	public final void startRemoteSystem(final String userId, final String remoteId) throws SystemManagerException {

		final User auth = authDao.getAuthData(userId);
		try {
			final Collection<Object> remote = dao.getSomeData(auth, "where id=" + remoteId);
			//operacion start de remote
			//remote.start();
		} catch (final OperationNotSupportedException e) {
			throw new SystemManagerException(e);
		}

	}

	public final void stopRemoteSystem(final String userId, final String remoteId) throws SystemManagerException {

		final User auth = authDao.getAuthData(userId);
		try {
			final Collection<Object> remote = dao.getSomeData(auth, "where id=" + remoteId);
			//operacion start de remote
			//remote.stop();
		} catch (final OperationNotSupportedException e) {
			throw new SystemManagerException(e);
		}

	}

	public final void addRemoteSystem(final String userId, final Object remote) throws SystemManagerException {

		final User auth = authDao.getAuthData(userId);
		boolean isAdded = false;
		try {
			isAdded = dao.updateSomeData(auth, remote);
		} catch (final OperationNotSupportedException e) {
			throw new SystemManagerException(e);
		}

		if (!isAdded) {
			throw new SystemManagerException("cannot add remote");
		}

	}

	public final void deleteRemoteSystem(final String userId, final String remoteId) throws SystemManagerException {
		//generamos un error.. sin querer siempre tenemos un usuario valido
		//final User auth = authDao.getAuthData(userId);
		final User auth = new User("1", "German", "Jimenez", "Madrid", new ArrayList<Object>());

		final boolean isDeleted = true;
		try {
			//otro error: no seteamos isDeleted
			dao.deleteSomeData(auth, remoteId);
		} catch (final OperationNotSupportedException e) {
			throw new SystemManagerException(e);
		}

		if (!isDeleted) {
			throw new SystemManagerException("cannot delete remote: does remote exists?");
		}
	}
}
