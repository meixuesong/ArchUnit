/*
 * Copyright 2017 TNG Technology Consulting GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tngtech.archunit.visual;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.EvaluationResult;

import static com.google.common.io.Files.copy;

public class Visualizer {

    private static final String JSON_FILE_NAME = "classes.json";
    private static final String VIOLATIONS_FILE_NAME = "violations.json";
    private static final String DIR = "report";

    public void visualize(JavaClasses classes, EvaluationResult evaluationResult, final File targetDir, VisualizationContext context) {
        targetDir.mkdirs();
        try (FileWriter classesWriter = new FileWriter(new File(targetDir, JSON_FILE_NAME))) {
            new JsonExporter().export(classes, classesWriter, context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (FileWriter violationsWriter = new FileWriter(new File(targetDir, VIOLATIONS_FILE_NAME))) {
            new JsonViolationExporter().export(evaluationResult, violationsWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            Files.walkFileTree(Paths.get(getClass().getResource(DIR).toURI()),
                    new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                            if (!path.toFile().isDirectory()) {
                                copy(path.toFile(), new File(targetDir, path.toFile().getName()));
                            }
                            return super.visitFile(path, basicFileAttributes);
                        }
                    });
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}