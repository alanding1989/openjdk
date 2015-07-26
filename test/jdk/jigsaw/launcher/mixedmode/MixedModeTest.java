/*
 * Copyright (c) 2014, 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/**
 * @test
 * @library ../../lib /lib/testlibrary
 * @modules jdk.jlink/jdk.tools.jmod
 * @build MixedModeTest CompilerUtils jdk.testlibrary.ProcessTools
 * @run testng MixedModeTest
 * @summary Test running with both a module path and class path
 */

import java.nio.file.Path;
import java.nio.file.Paths;

import static jdk.testlibrary.ProcessTools.*;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static org.testng.Assert.*;


@Test
public class MixedModeTest {

    private static final String TEST_SRC = System.getProperty("test.src");

    private static final Path SRC_DIR = Paths.get(TEST_SRC, "src");
    private static final Path MODS_DIR = Paths.get("mods");

    // the module name of the library module
    private static final String LIB_MODULE = "lib";

    // application source directory
    private static final String APP_SRC = "app";

    // application is compiled to classes
    private static final Path CLASSES_DIR = Paths.get("classes");

    // application main class
    private static final String MAIN_CLASS = "app.Main";


    @BeforeTest
    public void compile() throws Exception {

        // javac -d mods/$LIB_MODULE src/$LIB_MODULE/**
        boolean compiled = CompilerUtils.compile(
            SRC_DIR.resolve(LIB_MODULE),
            MODS_DIR.resolve(LIB_MODULE)
        );
        assertTrue(compiled, "library module did not compile");

        // javac -d classes -mp mods src/$APP_DIR/**
        compiled = CompilerUtils.compile(
            SRC_DIR.resolve(APP_SRC),
            CLASSES_DIR,
            "-mp", MODS_DIR.toString()
        );
        assertTrue(compiled, "app did not compile");
    }

    /**
     * Run application on class path that makes use of module on the
     * application module path. Uses -addmods.
     */
    public void runRunInMixedMode() throws Exception {

        // java -mp mods -addmods lib -cp classes app.Main
        int exitValue
            = executeTestJava("-mp", MODS_DIR.toString(),
                              "-addmods", LIB_MODULE,
                              "-cp", CLASSES_DIR.toString(),
                              MAIN_CLASS)
                .getExitValue();

        assertTrue(exitValue == 0);

    }

    /**
     * Run application on class path that makes use of module on the
     * application module path. Does not use -addmods and so will
     * fail at run-time.
     */
    public void runRunInMixedModeWithoutAddMods() throws Exception {

        // java -mp mods -cp classes app.Main
        int exitValue
            = executeTestJava("-mp", MODS_DIR.toString(),
                              "-cp", CLASSES_DIR.toString(),
                              MAIN_CLASS)
                .getExitValue();

        // CNFE or other error/exception
        assertTrue(exitValue != 0);

    }

}
