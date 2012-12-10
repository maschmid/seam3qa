package org.jboss.solder.config.xml.test.common.simple;

import static org.jboss.solder.config.xml.test.common.util.Deployments.baseDeployment;

import java.lang.reflect.Method;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.runner.RunWith;
import org.junit.Test;

/**
 * This test verifies that a no-namespace root element does not break the deployment.
 *
 * @author <a href="http://community.jboss.org/people/jharting">Jozef Hartinger</a>
 * @see https://issues.jboss.org/browse/SEAMXML-45
 */
@RunWith(Arquillian.class)
public class EmptyRootNamespaceTest {
    @Deployment(name = "EmptyRootNamespaceTest")
    public static Archive<?> deployment() {
        return baseDeployment(EmptyRootNamespaceTest.class, "empty-root-namespace-beans.xml")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addClasses(SimpleBeanTest.class, 
                    Bean1.class, Bean2.class, Bean3.class, 
                    ExtendedBean.class, ExtendedQualifier1.class, ExtendedQualifier2.class,
                    OverriddenBean.class, ScopeOverrideBean.class);
    }

    @Inject
    Bean1 x;

    @Inject
    Bean2 bean2;

    @Inject
    Bean3 y;

    @Test
    public void simpleBeanTest() {
        Assert.assertTrue(x != null);
        Assert.assertTrue(x.bean2 != null);

        Assert.assertEquals("test value", bean2.produceBean3);

        Assert.assertTrue(y != null);
        Assert.assertTrue("Post construct method not called", x.value == 1);
    }

    @Inject
    BeanManager manager;

    @Test
    public void testOverride() {
        Set<Bean<?>> beans = manager.getBeans(OverriddenBean.class);
        Assert.assertTrue(beans.size() == 1);
        Assert.assertTrue(beans.iterator().next().getName().equals("someBean"));

    }

    @Inject
    @ExtendedQualifier1
    @ExtendedQualifier2
    ExtendedBean ext;

    @Test
    public void testExtends() throws SecurityException, NoSuchMethodException {
        Assert.assertTrue(ext != null);
        Method method = ext.getClass().getDeclaredMethod("getData");
        method.getGenericReturnType();
    }
}
