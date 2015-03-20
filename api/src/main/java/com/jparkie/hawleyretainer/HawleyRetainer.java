package com.jparkie.hawleyretainer;

import android.app.Activity;

import com.jparkie.hawleyretainer.internal.Retainer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Field retention for objects which cannot be parceled nor serialized into a
 * {@link android.os.Bundle Bundle}. It utilizes a retained fragment
 * implementing the {@link java.util.Map Map} interface to provide identical
 * operations to a {@link android.os.Bundle Bundle}. It does not behave like a
 * Singleton class as it observes the lifecycle of its bound {@link android.app.Activity Activity},
 * thus, it destroys itself accordingly. This characteristic is useful to
 * prevent fields being retained in memory beyond the user-initiated destruction
 * of an {@link android.app.Activity Activity} like a Singleton while still
 * being retained during configuration changes. Utilize this class as a means
 * of simplifying the retention of certain expensive operations beyond
 * configuration changes while obeying user-initiated lifecycle events.
 * Nonetheless, as the retained objects cannot be parceled nor serialized, they
 * can still be claimed by the garbage collector resulting in the loss of data.
 * <pre>
 *     <code>
 *         public class ExampleActivity extends Activity {
 *             {@literal @}HawleyRetain
 *             Observable<Response> mNetworkResponse;
 *
 *             {@literal @}Override
 *             public void onCreate(Bundle savedInstanceState) {
 *                 super.onCreate(savedInstanceState);
 *                 HawleyRetainer.restoreRetainedObjectMap(this, this);
 *             }
 *
 *             {@literal @}Override
 *             public void onSaveInstanceState(Bundle outState) {
 *                 super.onSaveInstanceState(outState);
 *                 HawleyRetainer.saveRetainedObjectMap(this, this);
 *             }
 *         }
 *     </code>
 * </pre>
 * The methods can be called upon any target class as long as an Activity can
 * be specified along with it. The retainer will recursively traverse the
 * inheritance graph to inject all annotated fields.
 * Please remember that the support library variants of {@link android.app.Fragment Fragment}
 * and {@link android.app.Activity Activity} extend from them.
 * As a result, the methods do not have overloads.
 */
public final class HawleyRetainer {
    public static final String TAG = HawleyRetainer.class.getSimpleName();

    public static final String RETAINER_SUFFIX = "$$HawleyRetainer";
    public static final String RETAINER_ANDROID_PREFIX = "android.";
    public static final String RETAINER_JAVA_PREFIX = "java.";

    private static final Map<Class<?>, Retainer> CLASS_INJECTOR_MAP = new LinkedHashMap<>();

    private HawleyRetainer() {
        throw new AssertionError(TAG + ": Cannot be initialized.");
    }

    private static Retainer getRetainer(Class<?> cls) throws InstantiationException, IllegalAccessException{
        Retainer currentRetainer = CLASS_INJECTOR_MAP.get(cls);
        if (currentRetainer != null) {
            return currentRetainer;
        }

        final String clsName = cls.getName();
        if (clsName.startsWith(RETAINER_ANDROID_PREFIX) || clsName.startsWith(RETAINER_JAVA_PREFIX)) {
            return null;
        }

        try {
            final Class<?> injectorClass = Class.forName(clsName + RETAINER_SUFFIX);

            currentRetainer = (Retainer)injectorClass.newInstance();
        } catch (ClassNotFoundException e) {
            currentRetainer = getRetainer(cls.getSuperclass());
        }

        CLASS_INJECTOR_MAP.put(cls, currentRetainer);

        return currentRetainer;
    }

    private static <T extends Retainer> T safeGetRetainer(Object target, Retainer possibleRetainer) {
        try {
            final Class<?> targetClass = target.getClass();

            Retainer actualRetainer = getRetainer(targetClass);
            if (actualRetainer == null) {
                actualRetainer = possibleRetainer;
            }

            return (T) actualRetainer;
        } catch (Exception e) {
            throw new RuntimeException(TAG + ": Unable to get retained object map for the following: " + target, e);
        }
    }

    /**
     * Save all annotated fields into an unique internal retained fragment
     * committed to the specified {@link android.app.Activity Activity}.
     * Note: the garbage collector may reclaim the objects after this method is
     * called.
     *
     * @param target Target class from which a recursive traversal of its inheritance graph will be performed to map annotated fields.
     * @param activity {@link android.app.Activity Activity} utilized to provide the {@link android.app.FragmentManager FragmentManager} to commit the internal retained fragment map.
     */
    public static <T> void saveRetainedObjectMap(T target, Activity activity) {
        Retainer.Object<T> currentRetainer = safeGetRetainer(target, new Retainer.Object<T>());

        currentRetainer.saveRetainedObjectMap(target, activity);
    }

    /**
     * Restore all annotated fields from an unique internal retained fragment
     * committed to the specified {@link android.app.Activity Activity}.
     * Note: the garbage collector may reclaim the objects before this method
     * is called.
     *
     * @param target Target class from which a recursive traversal of its inheritance graph will be performed to map annotated fields.
     * @param activity Activity utilized to provide the {@link android.app.FragmentManager FragmentManager} to commit the internal retained fragment map.
     */
    public static <T> void restoreRetainedObjectMap(T target, Activity activity) {
        Retainer.Object<T> currentRetainer = safeGetRetainer(target, new Retainer.Object<T>());

        currentRetainer.restoreRetainedObjectMap(target, activity);
    }
}