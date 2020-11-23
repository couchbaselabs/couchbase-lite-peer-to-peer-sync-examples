//
// Copyright (c) 2020 Couchbase, Inc All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.couchbase.android.listsync.util;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public final class FileUtils {
    private FileUtils() {}

    public static boolean erase(@NonNull File file) { return deleteRecursive(file); }

    public static void unzipToDir(@NonNull InputStream in, @NonNull File destDir) throws IOException {
        final byte[] buffer = new byte[1024];

        try (ZipInputStream zis = new ZipInputStream(in)) {
            try {
                ZipEntry ze = zis.getNextEntry();
                while (ze != null) {
                    final File newFile = new File(destDir, ze.getName());
                    if (ze.isDirectory()) { makeDir(newFile); }
                    else if (!unzipFile(zis, buffer, newFile)) { continue; }
                    ze = zis.getNextEntry();
                }
            }
            finally {
                zis.closeEntry();
            }
        }
    }

    private static boolean deleteRecursive(@NonNull File file) {
        return !file.exists() || deleteContents(file) && file.delete();
    }

    private static boolean deleteContents(@NonNull File root) {
        if (!root.isDirectory()) { return true; }

        final File[] contents = root.listFiles();
        if (contents == null) { return true; }

        boolean succeeded = true;
        for (File file: contents) {
            if (!deleteRecursive(file)) { succeeded = false; }
        }

        return succeeded;
    }

    private static void makeDir(@NonNull File dir) throws IOException {
        if (!(dir.isDirectory() || dir.mkdirs())) { throw new IOException("Failed to create directory: " + dir); }
    }

    @SuppressWarnings("PMD.AvoidFileStream")
    private static boolean unzipFile(ZipInputStream zis, byte[] buffer, File newFile) throws IOException {
        final File parent = newFile.getParentFile();
        if (parent == null) { return false; }

        makeDir(parent);

        try (OutputStream fos = new FileOutputStream(newFile)) {
            int len;
            while ((len = zis.read(buffer)) > 0) { fos.write(buffer, 0, len); }
        }

        return true;
    }
}
