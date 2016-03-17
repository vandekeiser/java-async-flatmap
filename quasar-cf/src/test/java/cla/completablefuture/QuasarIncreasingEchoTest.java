package cla.completablefuture;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Increasing-Echo Quasar Text
 * @author circlespainter
 * 
 * Run with -ea -javaagent:"C:\Users\Claisse\.m2\repository\co\paralleluniverse\quasar-core\0.7.4\quasar-core-0.7.4-jdk8.jar"
 */
public class QuasarIncreasingEchoTest {
    @Test
    public void test() throws Exception {
        assertThat(QuasarIncreasingEchoApp.doAll(), is(10));
    }
}