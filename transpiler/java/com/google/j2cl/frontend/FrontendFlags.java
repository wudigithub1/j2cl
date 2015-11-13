/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.j2cl.frontend;

import com.google.j2cl.errors.Errors;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * The set of supported flags.
 */
public class FrontendFlags {

  @Option(
    name = "-omitfiles",
    metaVar = "<paths>",
    usage =
        "List of files that should be included in the compile but whose "
            + "output JS should not be kept because the user expects to provide a replacement."
  )
  protected String omitfiles = "";

  @Argument(metaVar = "<source files .java|.srcjar>", usage = "source files")
  protected List<String> files = new ArrayList<>();

  @Option(
    name = "-classpath",
    aliases = {"-cp"},
    metaVar = "<path>",
    usage = "Specify where to find user class files and annotation processors"
  )
  protected String classpath = "";

  @Option(
    name = "-sourcepath",
    metaVar = "<file>",
    usage = "Specify where to find input source files"
  )
  protected String sourcepath = "";
  
  @Option(
    name = "-nativesourcezip",
    metaVar = "<file>",
    usage = "Specify where to find zip file containing js impl files for native methods."
  )
  protected String nativesourceszippath = "";

  /**
   * Option that allows users to swap out the location of the JRE library.
   */
  @Option(
    name = "-bootclasspath",
    metaVar = "<path>",
    usage = "Override location of bootstrap class files"
  )
  protected String bootclasspath = "";

  @Option(
    name = "-d",
    metaVar = "<file>",
    usage = "Directory or zip into which to place generated files"
  )
  // TODO: replace with -output instead of -d
  protected String output = ".";

  @Option(
    name = "-encoding",
    metaVar = "<encoding>",
    usage = "Specify character encoding used by source files"
  )
  protected String encoding = System.getProperty("file.encoding", "UTF-8");

  @Option(
    name = "-source",
    metaVar = "<release>",
    usage = "Provide source compatibility with specified release"
  )
  protected String source = "1.8";

  @Option(name = "-h", aliases = "-help", usage = "print this message")
  protected boolean help = false;

  private final Errors errors;

  public FrontendFlags(Errors errors) {
    this.errors = errors;
  }

  /**
   * Parses the given args list and updates values.
   */
  public void parse(String[] args) {
    CmdLineParser parser = new CmdLineParser(this);
    try {
      parser.parseArgument(args);
      if (help) {
        parser.printUsage(System.out);
      }
    } catch (CmdLineException e) {
      String message = e.getMessage() + "\n";
      message += "Valid options: \n" + parser.printExample(OptionHandlerFilter.ALL);
      message += "\nuse -help for a list of possible options in more details";
      errors.error(Errors.Error.ERR_INVALID_FLAG, message);
    }
  }
}
