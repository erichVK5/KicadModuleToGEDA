// KicadModuleToGEDA - a utility for turning kicad modules to gEDA PCB footprints
// KicadModuleToGEDA.java v1.0
// Copyright (C) 2015 Erich S. Heinzle, a1039181@gmail.com

//    see LICENSE-gpl-v2.txt for software license
//    see README.txt
//    
//    This program is free software; you can redistribute it and/or
//    modify it under the terms of the GNU General Public License
//    as published by the Free Software Foundation; either version 2
//    of the License, or (at your option) any later version.
//    
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//    
//    You should have received a copy of the GNU General Public License
//    along with this program; if not, write to the Free Software
//    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
//    
//    KicadModuleToGEDA Copyright (C) 2015 Erich S. Heinzle a1039181@gmail.com



import java.util.Scanner;
import java.io.*;


public class KicadModuleToGEDA
{

	public static void main(String [] args) throws IOException
	{
                boolean insertElementPreliminaryComments = false;
		boolean useDefaultAuthorCredits = true;
		boolean verboseMode = false;
		boolean quietMode = false;
                boolean defaultHTMLsummary = true;
                boolean legacyKicadMmMetricUnits = false; // the usual legacy format is decimils
		boolean usingStdInForModule = false;
		long minimumViaAndDrillSizeNM = 0; // default is no minimum drill size
						   // in nanometres
						   // i.e. 300000 = 0.3mm = 11.81mil


// the following are default strings which can be changed to suit the user's needs,
// particularly if usage is intended via stdin, as these will be the defaults used
// when generating output files
		String htmlSummaryFileName = "HTMLsummary.html";
		String kicadModuleFileName = "kicad.mod";
		String moduleDescriptionText = " converted kicad module";
		String preliminaryCommentsFileName = "DefaultPrependedCommentsFile.txt";
                String convertedKicadModulePath = "Converted/";
                String htmlSummaryPathToConvertedModule = "kicad/footprints/";
		String defaultAuthorCreditsFileName = "AuthorCredits/DefaultFootprintPreliminaryText.txt";
		String tempStringArg = "";

		// first, we parse the command line arguments passed to the utility when started

		if (args.length == 0)
		{
			usingStdInForModule = true;
			quietMode = true;
		}
		else
		{
			for (int count = 0; count < args.length; count++)
			{
				if (verboseMode)
				{	
		                        System.out.println("\t" + args[count]);
				}
				if (args[count].startsWith("-k") && (count < (args.length - 1)))
				{
					count++;
					kicadModuleFileName = args[count];
					if (!quietMode)
					{
						System.out.println("Using " + args[count] +
						" as input file");
					}
				}
				else if (args[count].startsWith("-h") && (count < (args.length-1)))
				{
					count++;
					htmlSummaryFileName = args[count];
					if (verboseMode)
					{
						System.out.println("Using " + args[count] +
						" as HTML summary file");
						defaultHTMLsummary = false;
					}
				}
				else if (args[count].startsWith("-c") && (count < (args.length-1)))
				{
					useDefaultAuthorCredits = false;
					count++;
					preliminaryCommentsFileName = args[count];
					if (!quietMode)
					{
						System.out.println("Using " + args[count] +
						" for prepended element comments and author credits");
					}
				}
				else if (args[count].startsWith("-d") && (count < (args.length-1)))
				{
					count++;
					convertedKicadModulePath = args[count];
					if (verboseMode)
					{
						System.out.println("Using " + args[count] +
						" for converted modules");
					}
				}
				else if (args[count].startsWith("-s") && (count < (args.length-1)))
				{
					count++;
					moduleDescriptionText = args[count];
                                        if (verboseMode)
                                        {
                                                System.out.println("Using " + args[count] +
                                                " for HTML description of converted modules");
                                        }
				}
				else if (args[count].startsWith("-v"))
				{
					verboseMode = true;
					System.out.println("Verbose mode\n");
				}
				else if (args[count].startsWith("-q"))
				{
					quietMode = true;
				}
				else if (args[count].startsWith("-e"))
				{
					count++;
					minimumViaAndDrillSizeNM = Integer.parseInt(args[count]);
					if (!quietMode)
					{
						System.out.println("Using user specified " +
						"minimum via and drill size of " +
						minimumViaAndDrillSizeNM + " nanometres\n" +
						"equivalent to " +
						((float)minimumViaAndDrillSizeNM/1000000) +
						" mm and to " +
						((float)minimumViaAndDrillSizeNM/25400) +
						" mil (a.k.a. thousandths of an inch or 'thou').");
					}
				}
				else
				{
					System.out.println("\nUnknown option: " +
						args[count] + "\n\n");
					printHelpScreen();					
				}
			}
		}
		// having parsed the command line arguments, we proceed to process the data

		// we now come up with a more unique default HTML summary filename if a filename was
		// not specified at the command line
		if (defaultHTMLsummary)
		{
			// we replace any symbols in the Module path that will cause file IO conniptions
			htmlSummaryFileName = kicadModuleFileName.replaceAll("[^a-zA-Z0-9-]", "_") +
				"-" +  htmlSummaryFileName;
                        if (verboseMode)
                        {
				System.out.println("Using: " + htmlSummaryFileName + 
						" for HTML summary of converted modules");
                        }
		}

		Scanner kicadLibraryFile;
                File file1 = new File(kicadModuleFileName);
		if (!usingStdInForModule)
		{
// if the user specified a kicad module with command line arguments
// we will now look for the kicad module passed on the command line
//	                File file1 = new File(kicadModuleFileName);
        	        if (!file1.exists())
        	        {
        	                System.out.println("Hmm, the library file " + kicadModuleFileName + " was not found.");
        	                System.exit(0);
        	        }
                	kicadLibraryFile = new Scanner(file1);
		}
		else // we are using StdIn for the module, and args is of length one
		{
			kicadLibraryFile = new Scanner(System.in);
		}

// sort out the default preliminary licencing comments that will prepend the generated footprints  
		if (useDefaultAuthorCredits)
		{
			preliminaryCommentsFileName = defaultAuthorCreditsFileName;
		}

// we now look for the preliminary licencing and author credits file
                File file2 = new File(preliminaryCommentsFileName);
		if (!file2.exists())
		{
			System.out.println("Hmm, a preliminary comments file "
			+ preliminaryCommentsFileName + " was not found in the AuthorCredits directory...");
		}

// we get rid of the "kicad_modules/" at the front of the converted module filename
                if (kicadModuleFileName.startsWith("kicad_modules"))
                {
			kicadModuleFileName = kicadModuleFileName.substring(14);
		}

		String[] loadedLibraryStringArray = new String[59999];

		int loadedLibraryLineCounter = 0;
		int modulesInLibraryCount = 0;

		String tempString = "";
		Boolean legacyFlag = true;

                int extractedModuleLineCounter = 0;
                int extractedModuleCount = 0;
                Footprint[] footprintsInLibrary = new Footprint[100];
                float magnificationRatio = 1.0f;

		boolean firstLine = true;


// first of all, we load the library into a string array
// and count the number of lines
// and count the number of modules therein

		if (kicadLibraryFile.hasNext())
		{
			tempString = kicadLibraryFile.nextLine();
		}

		if (tempString.contains("(kicad_pcb") || tempString.contains("module"))
		{ // if so, it is either a standalone s-file module or a library of s-file modules 
			legacyFlag = false;
	                if (verboseMode)
        	        {
				System.out.println("For my next trick, s-file parsing");
			}
		} // otherwise, we just assume it is legacy format

	if (legacyFlag) // we will be processing a legacy format file
	{
		while (kicadLibraryFile.hasNext())
		{ // we do this in case the very first line is $MODULE, but it shouldn't be usually
//most modules start with an INDEX, so this should be safe
			if (firstLine)
//			if (loadedLibraryLineCounter == 0)
			{
				loadedLibraryStringArray[loadedLibraryLineCounter] = tempString;
				firstLine = false;
			}
			else // we continue loading lines into our string array
			// maybe we can dispense with this preliminary counting business
			{
				loadedLibraryStringArray[loadedLibraryLineCounter] = kicadLibraryFile.nextLine();			
			}

			if (loadedLibraryStringArray[loadedLibraryLineCounter].startsWith("$MODULE"))
			{
				modulesInLibraryCount++;
			} 
//			System.out.println(loadedLibraryStringArray[loadedLibraryLineCounter]);
			loadedLibraryLineCounter++;
//			System.out.println("Modules in library count: " + modulesInLibraryCount +
//					"\nLoaded library line cout: " + loadedLibraryLineCounter );
		}
	//	kicadLibraryFile.close();

// we create a string array to store individual module definitions

		String[] extractedModuleDefinition = new String[3000];
		boolean inModule = false;

		for (int counter = 0; counter < loadedLibraryLineCounter; counter++)
		{
			if (loadedLibraryStringArray[counter].startsWith("Units mm"))
			{
				legacyKicadMmMetricUnits = true;
			}

			// this has been added to allow a footprint to be magnified
			// it is not something supported by Kicad, but has been done
			// to allow families of related footprints to be generated
			// for gEDA PCB by simply adding a "Magnification X.xxx"
			// string near the beginning of a Kicad module
                        if (loadedLibraryStringArray[counter].startsWith("Magnification"))
                        {
                                magnificationRatio = Float.parseFloat(loadedLibraryStringArray[counter].substring(14, loadedLibraryStringArray[counter].length()));
				System.out.println("# Magnification ratio applied: " +
					 magnificationRatio);
                        }

			else if (loadedLibraryStringArray[counter].startsWith("$MODULE"))
			{
				inModule = true;
			}
			else if (loadedLibraryStringArray[counter].startsWith("$EndMOD"))
			{
				inModule = false;
				extractedModuleDefinition[extractedModuleLineCounter] =
					loadedLibraryStringArray[counter];
				// having found and extracted a module
				// we now store it in a footprint object
				if (verboseMode)
				{
					System.out.println("We've found " + extractedModuleCount
						+ " modules so far.");
				}
				// we convert the array of strings to one string
				// so that it can be passed to the Footprint object
				// we may be able to dispence with the array

				tempStringArg = "";
				for (int stringCounter = 0; stringCounter < extractedModuleLineCounter; stringCounter++)
				{
					tempStringArg = tempStringArg + "\n" +
						extractedModuleDefinition[stringCounter];
				}
                                footprintsInLibrary[extractedModuleCount] = new Footprint(tempStringArg, legacyKicadMmMetricUnits, minimumViaAndDrillSizeNM);
                                extractedModuleLineCounter = 0;
                                extractedModuleCount++;
				
			}

			if (inModule)
			{
                                extractedModuleDefinition[extractedModuleLineCounter] =
                                        loadedLibraryStringArray[counter];
				extractedModuleLineCounter++;
			}

		}	
	}
	else
	{
                if (verboseMode)
                {
			System.out.println("Not legacy, into the s-file parsing code");
	        }

                boolean inModule = false;
                boolean gotOneModule = false;

		int index = 0;
		int LeftBracketCount = 0;  // for s-file parsing
		int RightBracketCount = 0; // for s-file parsing
                int moduleBracketTally = 0;
                int elementBracketTally = 0; // positive is "(" bracket, negative is ")" bracket

	        String currentLine = "";
		String[] tokens;
		String parsedString = "";
		String parsedString2 = "";
		String completeModule = "";
		String trimmedModuleLine = "";

		while (kicadLibraryFile.hasNext())
		{
//			System.out.println(.nextLine());
			if (firstLine)
                        {
				currentLine = tempString;
// may in fact not need the next two lines
				loadedLibraryStringArray[loadedLibraryLineCounter] = tempString;
				loadedLibraryLineCounter++;
				firstLine = false;
                        }
			else
			{
				currentLine = kicadLibraryFile.nextLine();
			}
			parsedString = currentLine.trim();
			tokens = parsedString.split(" ");

			if (parsedString.contains("module "))
			{
		                if (verboseMode)
        		        {
					System.out.println("Module header found: " + currentLine);
				}
				moduleBracketTally = 0;
				elementBracketTally = -1; // negate the module start bracket
				inModule = true;
				modulesInLibraryCount++;
			}

			if (inModule)
			{
                                moduleBracketTally += tallyBracketsInString(parsedString);
				elementBracketTally += tallyBracketsInString(parsedString);

//				System.out.println("we start with element bracket tally of: "
//					+ elementBracketTally);
				while ((elementBracketTally > 0) && kicadLibraryFile.hasNext())
				{
					parsedString2 = kicadLibraryFile.nextLine();
					parsedString = currentLine + parsedString2.trim();
					elementBracketTally += tallyBracketsInString(parsedString2);
					moduleBracketTally += tallyBracketsInString(parsedString2);
				}
//				System.out.println("Updated element bracket tally of: "
//                       	 	               + elementBracketTally);
                                trimmedModuleLine = "";
				// we run split again in case we lengthened parsedString
                               	tokens = parsedString.split(" ");
				// we now assemble a single line of tokens for the single element
                                for (int count = 0; count < tokens.length; count++)
                                {
//					System.out.println(tokens[count]);
                                        parsedString = tokens[count].replaceAll("[()]", " ");
                         //               tokens[count] = parsedString.trim();
                                        trimmedModuleLine = trimmedModuleLine + " " +
					parsedString.trim(); // tokens[count];
                                }
				if (trimmedModuleLine.length() > 1)
				{
				//	System.out.println("Trimmed module element line:" + 
				//	trimmedModuleLine);
				}
				//we now add this to the completed module string with a carriage return
                                completeModule = completeModule + " " + 
					trimmedModuleLine.trim() + "\n";
				// we check to see if we are at the end of the module
                                if ((moduleBracketTally == 0) && inModule)
                                {
			                if (verboseMode)
        			        {
       	                        		System.out.println("End of Module");
               	                        }
					inModule = false;
                       	                gotOneModule = true;
	                                footprintsInLibrary[extractedModuleCount] = new Footprint(completeModule, legacyKicadMmMetricUnits, minimumViaAndDrillSizeNM);
					completeModule = "";
                                	extractedModuleCount++;
                               	}
			}
		}

	}
//	we close kicadLibaryFile, which wasn't used if stdin was the source of the module
//      and wwould have been used if the user specified a module filename 
        kicadLibraryFile.close(); // we'll try it down here



// we now have finished parsing the library file, and we have an array of footprint objects
// that we can interogate, namely:  footprintsInLibrary[extractedModuleCount] 

	        if (verboseMode)
                {
			System.out.println("Just closed the open file, now counting to: " + 
				extractedModuleCount + " - the extracted module count\n" +
				"versus counted modules in library: " + modulesInLibraryCount);
		}

// we can now step through the array of footprints we generated from the kicad module(s)
// we generate a GEDA format footprint for each of them, save each one to a module_name.fp,
// and create a gedasymbols.org compatible HTML segment for inclusion in a user index 

		// we insert a heading for the HTML summary
                String HTMLsummaryOfConvertedModules = "<html><h2>" +
                	kicadModuleFileName + "</h2>\n";

		for (int counter = 0; counter < extractedModuleCount; counter++)
		{
			if (verboseMode)
			{
				System.out.println("Footprint object array index: " + counter);
			}

			// we generate a string containing the GEDA footprint filename
			String outputFileName = footprintsInLibrary[counter].generateGEDAfootprintFilename();

			// we then append a listing for this particular footprint
			// to the HTML summary
			HTMLsummaryOfConvertedModules = HTMLsummaryOfConvertedModules +
				"<li><a href=\"" +
				htmlSummaryPathToConvertedModule +
				kicadModuleFileName + "/" +
				outputFileName + "\"> " +
				outputFileName + " </a> - " +
				moduleDescriptionText +
				" </li>\n";

			if (!quietMode)
			{
				System.out.println(outputFileName);
			}

// a String variable to contain the footprint description
			String footprintData = "";

// we start by prepending some preliminaries, which can include author credit
// as well as licencing information, and use either the default or user
// supplied footprint preliminary comments file 

                       	if (!file2.exists())
                        {
       	                        footprintData = "# No element preliminaries text file found";
               	        }
                        else
       	                {
               	                Scanner footprintPreliminaryComments = new Scanner(file2);
                       	        while (footprintPreliminaryComments.hasNext())
                               	{
                                        footprintData = footprintData + footprintPreliminaryComments.nextLine() + "\n";
       	                        }
				footprintPreliminaryComments.close();
                       	}

			// we now append a generated element header and it's fields
			footprintData = footprintData +
				footprintsInLibrary[counter].generateGEDAfootprint(magnificationRatio);

			// this is where we could insert some Attribute fields
//                        System.out.println("Attribute(use-licence \"GPLv3\")");
//                        System.out.println("Attribute(dist-licence \"unlimited\")");
//                        System.out.println("Attribute(author xxxx)"); // default licence is GPLv3
			// but ? PCB breaks when these are inserted due to
			// ? bad formatting or ? a missing share/pcb/listLibraryContents.sh file

			// and we now finish off the footprint element with a bracket
			footprintData = footprintData + ")";

			if (verboseMode)
			{
	                        System.out.println(footprintData);
				// and we now use the toString method to return the module text
				System.out.println("\n\n" + footprintsInLibrary[counter] + "\n\n");
			}

			// we now create a file with the name of the module and conversion directory
			// path prepended
                        PrintWriter newGEDAfootprintFile = new PrintWriter(convertedKicadModulePath +
                                outputFileName);
			// we write the completed GEDA footprint to the filesystem
                        newGEDAfootprintFile.println(footprintData);
			// and then close the file
			newGEDAfootprintFile.close();
		}

// having populated footprint objects in an array
// we now finish off the HTML summary of the created modules

		HTMLsummaryOfConvertedModules = HTMLsummaryOfConvertedModules + "\n</ul></html>\n";
		if (verboseMode)
		{
			System.out.println(HTMLsummaryOfConvertedModules);
		}

// and we pass the HTML to a subroutine to save the summary to disc, using either a user
// supplied file name, or alternatively,  an auto generated name kicad_module_name-HTMLsummary.html

		generateHTMLsummaryFile(convertedKicadModulePath, htmlSummaryFileName, HTMLsummaryOfConvertedModules);
		
	}

// we have a routine to output completed GEDA footprint files into the conversion directory
// by interrogating the array of footprints we have created: 
//            footprintsInLibrary[extractedModuleCount] 

	private static void writeGEDAfootprints(Footprint [] footprintArray) throws IOException
	{
		System.out.println("Placeholder footprint writer stub");
	}

// we have a routine to put the completed HTML summary into a file in the same directory as
// the converted modules
// the HTML file is formatted to be easily inserted into a gedasymbols.org user's summary index

        private static void generateHTMLsummaryFile(String conversionDir, String HTMLfileName, String HTMLmoduleSummary) throws IOException
        {
		// we generate an appropriate HTML summary file name
                String fileName = conversionDir + HTMLfileName;
                PrintWriter HTMLmoduleSummaryFile = new PrintWriter(fileName);
                HTMLmoduleSummaryFile.println(HTMLmoduleSummary);
                HTMLmoduleSummaryFile.close();
        }


// we need this method to see if we are at the end of an s-file module, and to see if we
// are at the end of an s-file element, by allowing us to keep a running tally of the number
// of L "(" and R ")" brackets

	private static int tallyBracketsInString(String arg)
	{
		int leftBrackets = 0;
		int rightBrackets = 0;

		for (int stringIndex = 0; stringIndex < arg.length(); stringIndex++)
		{
			if (arg.charAt(stringIndex) == '(')
			{
				leftBrackets++;
			}
			else if (arg.charAt(stringIndex) == ')')
			{
				rightBrackets++;
			}
		}
		return (leftBrackets - rightBrackets);
	}


	public static void printHelpScreen()
	{
		System.out.println("\nUsage:\n\n" +
				"user@box:~$ java KicadPadDriver " +
				"-q quietMode " +
				"-k foo.mod " +
				"-c PrependedAuthorCreditsCommentsLicenceEtc.txt " +
				"-h HTMLsummaryOfFootprintsOutputFileName.html " +
				"-d destinationDirectoryPathForConvertedModuleDirectory " +
				"-s summaryDescriptionOfmoduleOrModules " +
				"-v verboseOutputToStdOut\n" );

                System.out.println("Options are:\n\n" +
                                "\t -q QuietMode\n" +
                                "\t\t Default is not quiet mode," +
                                        " with a simple summary of progress provided\n" +
				"\t -k kicadmodule.mod\n" +
				"\t\t parses legacy & s-file format modules in decimil or mm units\n" +
                                "\t -h HTMLsummaryOutputFile.html\n" +
				"\t\t Default is: \"HTMLsummary.html\"\n" + 
                                "\t -c PrependedElementComments.txt\n" +
				"\t\t Default is:" +
				"   ./AuthorCredits/DefaultPrependedCommentsFile.txt\n" +
				"\t -d DestinationdirForConvertedModules\n" +
				"\t\t Default is:   ./Converted/\n" +
				"\t -s SummaryOfModuleOrModulesForHTML\n" +
				"\t\t Default inserted in HTML is: \"converted Kicad module\"\n" +
				"\t -v VerboseMode\n" +
				"\t\t Default is not verbose\n" );

		System.out.println("Example:\n\n" +
				"user@box~$ java KicadPadDriver -q -k " +
				"kicad_modules/vacuum_tubes.mod -h vacuum_tubes.html " +
				"-c AuthorCredits/FootprintPreliminaryTextOSHEC.txt " +
				"-s \"Vacuum Tube\" -d \"Converted/\"\n\n");
	}

}
