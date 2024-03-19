package com.goodobj.util;

import java.io.File;

/**
 * @author yuanlei-003
 */
public class FileUtils {

    public static void ls(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            assert files != null;
            for (File f : files) {
                if (f.isFile()) {
                    System.out.println(f);
                } else {
                    ls(f);
                }
            }
        }
    }
}
