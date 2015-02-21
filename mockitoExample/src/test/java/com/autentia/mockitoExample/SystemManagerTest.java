package com.autentia.mockitoExample;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import javax.naming.OperationNotSupportedException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnit44Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.autentia.mockitoExample.dao.AuthDAO;
import com.autentia.mockitoExample.dao.GenericDAO;
import com.autentia.mockitoExample.dao.User;

//runner de mockito que detecta las anotaciones
@RunWith(MockitoJUnit44Runner.class)
public class SystemManagerTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemManagerTest.class);

    // generamos un mock con anotaciones
    @Mock
    private AuthDAO mockAuthDao;

    // generamos un mock mediante el metodo mock
    private final GenericDAO mockGenericDao = mock(GenericDAO.class);

    // variable inOrder de mockito para comprobar llamadas en orden
    private InOrder ordered;

    // el manager a testear
    private SystemManager manager;

    // Un usuario valido, para pruebas
    private static final User validUser = new User("1", "German", "Jimenez", "Madrid", new ArrayList<Object>());

    // Un usuario invalido, para pruebas
    private static final User invalidUser = new User("2", "Autentia", "Autentia", "San Fernando de Henares", null);

    // id valido de sistema
    private static final String validId = "12345";
    // id invalido de sistema
    private static final String invalidId = "54321";

    /**
     * Inicializamos cada una de las pruebas
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        LOGGER.info("instanciamos el manager con los mock creados");
        manager = new SystemManager(mockAuthDao, mockGenericDao);

        LOGGER.info("stubbing del mock del DAO de autenticacion");
        // solo hacemos stubbiong delos métodos copn datos que nos interesan
        // no toiene sentido simular TODA la funcionalidad del objecto del que hacemos mocks
        when(mockAuthDao.getAuthData(validUser.getId())).thenReturn(validUser);
        when(mockAuthDao.getAuthData(invalidUser.getId())).thenReturn(null);

        LOGGER.info("stubbing del mock del DAO de acceso a los datos de sistemas");
        when(mockGenericDao.getSomeData(validUser, "where id=" + validId)).thenReturn(new ArrayList<Object>());
        when(mockGenericDao.getSomeData(validUser, "where id=" + invalidId)).thenThrow(new OperationNotSupportedException());

        LOGGER.info("usamos argument matchers para el filtro pues nos da igual");
        when(mockGenericDao.getSomeData((User) eq(null), anyObject())).thenThrow(new OperationNotSupportedException());

        when(mockGenericDao.deleteSomeData(validUser, "where id=" + validId)).thenReturn(true);
        when(mockGenericDao.deleteSomeData(validUser, "where id=" + invalidId)).thenReturn(true);
        when(mockGenericDao.deleteSomeData((User) eq(null), anyString())).thenReturn(true);

        LOGGER.info("primero debe ejecutarse la llamada al dao de autenticación, y despues el de acceso a datos del sistema (la validaciones del orden en cada prueba");
        ordered = inOrder(mockAuthDao, mockGenericDao);
    }

    /**
     * Prueba que un sistema deberia inicializarse con un usuario y sistema
     * validos
     *
     * @throws Exception
     */
    @Test
    public void testShouldStartRemoteSystemWithValidUserAndSystem() throws Exception {
    	LOGGER.info("llamada al api a probar");
        manager.startRemoteSystem(validUser.getId(), validId);

        LOGGER.info("vemos si se ejecutan las llamadas pertinentes a los dao, y en el orden correcto");
        ordered.verify(mockAuthDao).getAuthData(validUser.getId());
        ordered.verify(mockGenericDao).getSomeData(validUser, "where id=" + validId);
    }

    /**
     * Prueba que un sistema no se puede iniciar debido a un usuario invalido
     *
     * @throws Exception
     */
    @Test(expected = SystemManagerException.class)
    public void testShouldFailStartRemoteSystemWithInvalidUser() throws Exception {
        try {
        	LOGGER.info("llamada al api a probar");
            manager.startRemoteSystem(invalidUser.getId(), validId);
            fail("cannot work with invalid user");
        } catch (final SystemManagerException e) {
        	LOGGER.info("vemos si se ejecutan las llamadas pertinentes a los dao, y en el orden correcto");
            ordered.verify(mockAuthDao).getAuthData(invalidUser.getId());
            ordered.verify(mockGenericDao).getSomeData((User) eq(null), anyObject());
            throw e;
        }

    }

    /**
     * Prueba que un sistema no se puede iniciar debido a un sistema inexistente
     *
     * @throws Exception
     */
    @Test(expected = SystemManagerException.class)
    public void testShouldFailStartRemoteSystemWithValidUserAndInvalidSystem() throws Exception {
        try {
        	LOGGER.info("llamada al api a probar");
            manager.startRemoteSystem(validUser.getId(), invalidId);
            fail("cannot work with invalid system");
        } catch (final SystemManagerException e) {
        	LOGGER.info("vemos si se ejecutan las llamadas pertinentes a los dao, y en el orden correcto");
            ordered.verify(mockAuthDao).getAuthData(validUser.getId());
            ordered.verify(mockGenericDao).getSomeData(validUser, "where id=" + invalidId);
            throw e;
        }
    }

    /**
     * Prueba que un sistema se elimina correctamente
     *
     * @throws Exception
     */
    @Test
    public void testShouldDeleteRemoteSystemWithValidUserAndSystem() throws Exception {
    	LOGGER.info("llamada al api a probar");
        manager.deleteRemoteSystem(validUser.getId(), validId);
        LOGGER.info("vemos si se ejecutan las llamadas pertinentes a los dao, y en el orden correcto");
        ordered.verify(mockAuthDao).getAuthData(validUser.getId());
        ordered.verify(mockGenericDao).getSomeData(validUser, "where id=" + validId);
    }

    /**
     * Prueba que un sistema no se puede borrar debido a un usuario invalido
     *
     * @throws Exception
     */
    @Test(expected = SystemManagerException.class)
    public void testShouldFailDeleteRemoteSystemWithInvalidUser() throws Exception {
        try {
        	LOGGER.info("llamada al api a probar");
            manager.deleteRemoteSystem(invalidUser.getId(), validId);
            fail("cannot work with invalid user, but method doesn't fails");
        } catch (final SystemManagerException e) {
            // vemos si se ejecutan las llamadas pertinentes a los dao, y en el
            // orden correcto
            ordered.verify(mockAuthDao).getAuthData(invalidUser.getId());
            ordered.verify(mockGenericDao).getSomeData((User) eq(null), anyObject());
            throw e;
        }

    }

    /**
     * Prueba que un sistema no se puede borrar debido a un sistema invalido
     *
     * @throws Exception
     */
    @Test(expected = SystemManagerException.class)
    public void testShouldDeleteStartRemoteSystemWithValidUserAndInvalidSystem() throws Exception {
        try {
        	LOGGER.info("llamada al api a probar");
            manager.deleteRemoteSystem(validUser.getId(), invalidId);
            fail("cannot work with invalid system, but method doesn't fails");
        } catch (final SystemManagerException e) {
        	LOGGER.info("vemos si se ejecutan las llamadas pertinentes a los dao, y en el orden correcto");
            ordered.verify(mockAuthDao).getAuthData(validUser.getId());
            ordered.verify(mockGenericDao).getSomeData(validUser, "where id=" + invalidId);
            throw e;
        }
    }
}
