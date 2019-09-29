package de.ohmesoftware.xposed.fullstacktrace;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Logs the full stack trace instead of shortening it or hiding it like UnknownHostException
 * <p>
 * Does not apply to a special app or package, but is used system wide.
 * </p>
 * See http://androidxref.com/9.0.0_r3/xref/frameworks/base/core/java/android/util/Log.java
 *
 * @author <a href="mailto:k_o_@sourceforge.net">Karsten Ohme
 * (karsten@simless.com)</a>
 */
public class FullStackTrace implements IXposedHookLoadPackage {

    private static final String TAG = "FullStackTrace";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Log.d(TAG, String.format("Loaded: %s", lpparam.packageName));

        XposedHelpers.findAndHookMethod(
                "android.util.Log",
                lpparam.classLoader, "printlns",
                int.class, int.class, String.class, String.class, Throwable.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.args[4] != null) {
                            Throwable tr = ((Throwable)param.args[4]);
                            Log.println((int)param.args[1], (String)param.args[2],
                                    String.format("Full exception stack trace:\n %s", Log.getStackTraceString(tr)));
                        }
                    }
                }
        );

        XposedHelpers.findAndHookMethod(
                "android.util.Log",
                lpparam.classLoader, "getStackTraceString",
                Throwable.class,
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        Throwable tr = (Throwable) param.args[0];
                        if (tr == null) {
                             return "";
                        }
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw, false);
                        tr.printStackTrace(pw);
                        pw.flush();
                        return sw.toString();
                    }

                }
        );

    }

}
