/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.tasks;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.PrintWriter;
import java.io.StringReader;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.console.options.Theme;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.fakes.TestDescriptorStub;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

class ColorPaletteTest {
	@Nested
	class LoadFromProperties {
		@Test
		void singleOverride() {
			String properties = """
					SUCCESSFUL = 35;1
					""";
			ColorPalette colorPalette = new ColorPalette(new StringReader(properties));

			String actual = colorPalette.paint(Style.SUCCESSFUL, "text");

			Assertions.assertEquals("\u001B[35;1mtext\u001B[0m", actual);
		}

		@Test
		void keysAreCaseInsensitive() {
			String properties = """
					suCcESSfuL = 35;1
					""";
			ColorPalette colorPalette = new ColorPalette(new StringReader(properties));

			String actual = colorPalette.paint(Style.SUCCESSFUL, "text");

			Assertions.assertEquals("\u001B[35;1mtext\u001B[0m", actual);
		}

		@Test
		void junkKeysAreIgnored() {
			String properties = """
					SUCCESSFUL = 35;1
					JUNK = 1;31;40
					""";
			ColorPalette colorPalette = new ColorPalette(new StringReader(properties));

			String actual = colorPalette.paint(Style.SUCCESSFUL, "text");

			Assertions.assertEquals("\u001B[35;1mtext\u001B[0m", actual);
		}

		@Test
		void multipleOverrides() {
			String properties = """
					SUCCESSFUL = 35;1
					FAILED = 33;4
					""";
			ColorPalette colorPalette = new ColorPalette(new StringReader(properties));

			String successful = colorPalette.paint(Style.SUCCESSFUL, "text");
			String failed = colorPalette.paint(Style.FAILED, "text");

			Assertions.assertEquals("\u001B[35;1mtext\u001B[0m", successful);
			Assertions.assertEquals("\u001B[33;4mtext\u001B[0m", failed);
		}

		@Test
		void unspecifiedStylesAreDefault() {
			String properties = """
					SUCCESSFUL = 35;1
					""";
			ColorPalette colorPalette = new ColorPalette(new StringReader(properties));

			String actual = colorPalette.paint(Style.FAILED, "text");

			Assertions.assertEquals("\u001B[31mtext\u001B[0m", actual);
		}

		@Test
		void cannotOverrideNone() {
			String properties = """
					NONE = 35;1
					""";

			Assertions.assertThrows(IllegalArgumentException.class,
				() -> new ColorPalette(new StringReader(properties)));
		}
	}

	@Nested
	class DemonstratePalettes {
		@Test
		void verbose_default() {
			PrintWriter out = new PrintWriter(System.out);
			TestExecutionListener listener = new VerboseTreePrintingListener(out, ColorPalette.DEFAULT(), 16,
				Theme.ASCII);

			demoTestRun(listener);

			assertDoesNotThrow(out::flush);
		}

		@Test
		void verbose_single_color() {
			PrintWriter out = new PrintWriter(System.out);
			TestExecutionListener listener = new VerboseTreePrintingListener(out, ColorPalette.SINGLE_COlOR(), 16,
				Theme.ASCII);

			demoTestRun(listener);

			assertDoesNotThrow(out::flush);
		}

		@Test
		void simple_default() {
			PrintWriter out = new PrintWriter(System.out);
			TestExecutionListener listener = new TreePrintingListener(out, ColorPalette.DEFAULT(), Theme.ASCII);

			demoTestRun(listener);

			assertDoesNotThrow(out::flush);
		}

		@Test
		void simple_single_color() {
			PrintWriter out = new PrintWriter(System.out);
			TestExecutionListener listener = new TreePrintingListener(out, ColorPalette.SINGLE_COlOR(), Theme.ASCII);

			demoTestRun(listener);

			assertDoesNotThrow(out::flush);
		}

		@Test
		void flat_default() {
			PrintWriter out = new PrintWriter(System.out);
			TestExecutionListener listener = new FlatPrintingListener(out, ColorPalette.DEFAULT());

			demoTestRun(listener);

			assertDoesNotThrow(out::flush);
		}

		@Test
		void flat_single_color() {
			PrintWriter out = new PrintWriter(System.out);
			TestExecutionListener listener = new FlatPrintingListener(out, ColorPalette.SINGLE_COlOR());

			demoTestRun(listener);

			assertDoesNotThrow(out::flush);
		}

		private void demoTestRun(TestExecutionListener listener) {
			TestDescriptor testDescriptor = new TestDescriptorStub(UniqueId.forEngine("demo-engine"), "My Test");
			TestPlan testPlan = TestPlan.from(Lists.newArrayList(testDescriptor));
			listener.testPlanExecutionStarted(testPlan);
			listener.executionStarted(TestIdentifier.from(testDescriptor));
			listener.executionFinished(TestIdentifier.from(testDescriptor), TestExecutionResult.successful());
			listener.dynamicTestRegistered(TestIdentifier.from(testDescriptor));
			listener.executionStarted(TestIdentifier.from(testDescriptor));
			listener.executionFinished(TestIdentifier.from(testDescriptor),
				TestExecutionResult.failed(new Exception()));
			listener.executionStarted(TestIdentifier.from(testDescriptor));
			listener.executionFinished(TestIdentifier.from(testDescriptor),
				TestExecutionResult.aborted(new Exception()));
			listener.reportingEntryPublished(TestIdentifier.from(testDescriptor), ReportEntry.from("Key", "Value"));
			listener.executionSkipped(TestIdentifier.from(testDescriptor), "to demonstrate skipping");
			listener.testPlanExecutionFinished(testPlan);
		}
	}
}
