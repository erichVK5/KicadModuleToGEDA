# KicadModuleToGEDA
KicadModuleToGEDA - a utility for turning kicad modules into gEDA PCB footprints

README.md v1.0
Copyright (C) 2015 Erich S. Heinzle, a1039181@gmail.com

    see LICENSE-gpl-v2.txt for software license


This utility has been written to enable gEDA PCB users to convert Kicad 
footprints, known as modules, to gEDA PCB compatible foootprint files.

The term Kicad "module" and the PCB term "footprint" can be used fairly
interchangeably, although a Kicad module "foo.mod" may contain one or
multiple distinct device footprint definitions, unlike a gEDA PCB footprint
file which describes only one device.

This utility can process Kicad module files containing one or many footprint
definitions using command line options or via stdin.

When passed modules via stdin, the utility expects there to be a 
./Conversion
directory for the saved files, and will default to quiet mode and use
default settings for HTML summary filenames, and assume that pin
and pad minimum sizes are zero nanometres.

The utility parses Kicad modules and converts decimils and mm to nanometres
for further manipulation prior to exporting the PCB footprint definition.
Those seeking to implement conversion of Kicad modules to GEDA PCB footprints
in their own code can look at/use the conversion logic in the Pad, DrawnElement,
Arc, and Circle classes, as well as the FootprintHeader, all of which extend
the FootprintElementArchetype class, as well as the Footprint class.

Why java? Write once, run anywhere, plus I needed a practical task to
become more familiar with java. Furthermore, I did not envisage the need for
C code which could be integrated into PCB for on the fly conversion, since the
module, once converted, can join existing libraries of PCB footprints in
perpetuity, and converted footprints warrant some vetting before use anyway.


Installation:

- install a java compiler and java virtual machine (JVM) using your preferred
package management system/source, if it isn't already installed.

- clone the KicadModuleToGEDA git repository (this should be simple, after all,
you already build the most current stable gEDA PCB release from the git
repository.... don't you?). Failing that, download the java source code and
put them in a suitable directory with the same subdirectories and contents.

- in the KicadModuleToGEDA directory, type:

user@box:~$  javac \*.java

and that should be it, you are now ready to use the KicadModuleToGEDA utility.


Features:

- kicad mm and decimil dimensioned module formats are supported
- the utility reproduces kicad's drawn segments on the copper layers as pads
with no soldermask clearance.
- the utility will identify and convert all modules described in a module file
into distinct PCB foootprints
- the utility will convert "obround" pads which have a pin in addition to a
rectangular or ovoid pad that surrounds the pin on the top and bottom copper layer
- the recommended 0.01mil square bracket format is used for generated gEDA
PCB footprints
- an HTML summary file is automatically generated that is compatible with
the formatting used within the user indices on http://www.gedasymbols.org
- support has been implemented for a "magnification" or scaling feature
that enables families of silkscreen layouts to be generated at different
sizes. This has been found to be useful for generating families of
seven, sixteen or fourteen segment LED displays, for example.
- magnification and translation applies to silk screen arcs, circles, 
lines, and also pads drawn on the copper as drawn line elements
- pins and pads can be translated but magnification does not affect them
- users can specify a minimum via/pin drill size during conversion
- Text on the silk layer has now been implemented, using the free Hershey
stroked font, rendered as line segments on the silkscreen layer, based
on the text field descriptors in the Kicad module. Rotated text is not
yet supported.

Deliberate omissions due to a lack of PCB equivalents:

- 3D rendering information is ignored - aka "3D Wings" files.
- Very rudimentary support has been implemented for rotated pads/pins,
i.e. rotation is made modulo 90 degrees, and similarly,
only rudimentary support for rotated text alignments, since Kicad
supports arbitrary element rotation in the module definition, but
PCB does not.
- bezier curves can be defined in an s-file module definition, but
any such definitions will be ignored by the KicadModuleToGEDA utility.


Known issues:

- some kicad modules converted from Eagle to Kicad with the Eagle2Kicad.ulp
utility have faulty arc definitions which manifest as properly centred arcs
that have incorrect start and finish positions. Some hand tweaking is needed
either in the errant module or the final PCB footprint, depending on which
format you are more comfortable with in the text editor.
- some kicad modules converted from eagle have had octagonal pads converted
to kicad obround pads with a length twice that of the width. If these are
then converted to PCB footprints, closely spaced pads/pins may overlap, but
the offending pad definitions causing overlaps can easily be removed from
the footprint definition with a text editor


Background:

So called "legacy" modules are the soon to be deprecated format for kicad
footprints. Kicad developers have been implementing changes in rendering and
file formats, and the new file format supported by Kicad is the "s-file" format.

Kicad "Libraries" can contain modules, symbols and schematics for a particular
project. Modules can be extracted from zipped Libraries.

The utility supports both legacy and s-file format modules, and determines what
format the module is during conversion.


Useful links:

http://www.gedasymbols.org
   - get yourself a CVS account - you'll then be able to share your footprints with other PCB users

http://www.kicadlib.org
   - lots to choose from, many are GPL

http://library.oshec.org
   - extensive collection, but some automatically converted arc definitions are in error and
   need tweaking due, it seems, to a glitch in the Eagle2Kicad.ulp utility

http://smisioto.no-ip.org/elettronica/kicad/kicad-en.html
   - a large selection of OHW module definitions. Scroll past the symbol libraries...


Usage:

user@box:~$ java KicadModuleToGEDA -v verboseOutputToStdOut -q quietMode -k foo.mod -c PrependedAuthorCreditsCommentsLicenceEtc.txt -h HTMLsummaryOfFootprintsOutputFileName.html -d destinationDirectoryPathForConvertedModuleDirectory -s summaryDescriptionOfmoduleOrModules

or:

user@box:~$ java KicadModuleToGEDA < kicadModule.mod


Options are:

	 -q QuietMode
		 Default is not quiet mode, with a simple summary of progress provided
         -v VerboseMode
                 Default is not verbose
	 -k kicadmodule.mod
		 parses legacy format modules in default decimil or mm units
	 -h HTMLsummaryOutputFile.html
		 Default is: "HTMLsummary.html"
	 -c PrependedElementComments.txt
		 Default is:   AuthorCredits/DefaultPrependedCommentsFile.txt
	 -d DestinationdirForConvertedModules
		 Default is:   Converted/
	 -s SummaryOfModuleOrModulesForHTML
		 Default is: "converted Kicad module"
	 -e enforceMinimumDrillAndViaSize
		 Specified in nanometres, default is: 0 nanometres

Example of use:

user@box~$ java KicadModuleToGEDA -q -k kicad_modules/vacuum_tubes.mod -h vacuum_tubes.html -c AuthorCredits/FootprintPreliminaryTextOSHEC.txt -s "Vacuum Tube" -d "Converted/"

or

user@box~$ java KicadModuleToGEDA < kicad_modules/vacuum_tubes.mod



For the adventurous....

How to create multiple +/- magnified/shrunken silkscreen layers:

to create a magnified silkscreen layer, add a "Magnification X.XXX" command
to the $INDEX section of a legacy kicad module, i.e.

	$INDEX
	Magnification 1.2
	led-MSA5XXX
	$EndINDEX
	....
	$EndModule


This will be recognised during parsing by the module conversion utility
and all silk screen elements including arcs, circles and drawn lines
will be enlarged by a factor of X.XXX, and in the example above, by a
factor of 1.2 

Kicad allows copper lines to be defined in DS drawn segment descriptors
and these are also magnified along with silk screen elements

To create offset silkscreen elements, perhaps to combine with another
set of silkscreen elements, i.e. for a pair of seven segment displays,
duplicate the contents of the $MODULE, modify its name, and append it
to the existing module file

	...
	$EndMODULE MSA5XXX
	$MODULE led-MSA5XXX-seconddigit
	Po 4630 0 0 15 00200000 00000000 ~~
	Li led-MSA5XXX
	....
	//identical bits not shown here for brevity
	....
	$EndMODULE

In addition, an X and Y offset can be specified, in this case, it
is specified in the standard kicad module instruction

	"Po 4630 0 0 15 00200000 00000000 ~~"

which in this case describes an X offset of "4630", and Y offset
of zero, which for a decimil module is an X displacement of 463 mil.,
and after magnification of 1.2 times, will equal an X displacement of
555.6 mil.  

Importantly, any X or Y offset must be divided by the magnification
ratio in use before insertion into the modified .mod file, as
magnification applied to the final coordinates will also affect the
X and Y offsets. i.e., in the previous example, if an X offset of
463 mil is the actual offset required, and magnification is
specified as 1.2 times, the offset inserted into the module will
be 4630/1.2 = 3858

The pair of module definitions with distinct names and distinct
X,Y offsets residing in the same module file can now be converted
by the utility.

The utility will generate two footprint files, the elements of which
can be combined with a text editor to result, in this case, in a pair
of 7 segment display silkscreen layers, the centres of which are
separated horizontally by 463mil.

The pads or pins can then be fairly easily placed with hand modification
of the definition file, now that the hard work of the silkscreen
has been done.

This technique of placing multiple module definitions in the same file
for processing with user specified X,Y offsets and/or magnification
is essentially a scriptable process for generating more complex
footprints, and there is nothing to stop 3, 4 or 8 segment displays,
for example, from being generated.

Two example files are in the Transmogrify directory

	LED-DISPLAY-ORIGINAL.mod
	LED-DISPLAY-MAGNIFY-TRANSLATE-EXAMPLE.mod

the second file simply contains four copies of the first file, with a single
$INDEX ... $EndINDEX section at the beginning into which a Magnification directive
has been placed, and the second, third, and fourth copies have had varying amounts
of X and Y translation effected in their respective "Po ....." position 
definitions, and distinct names provided for each of the four copies of the module
undergoing conversion, to demonstrate how the magnification and translation option
can be used

You can see the difference between the output for the original .mod file by
comparing the results of:

	user@box~$ java KicadModuleToGEDA -k Transmogrify/LED-DISPLAY-ORIGINAL.mod

which produces a single footprint file led-MSA5XXX.fp in the ./Converted directory

with:

	user@box~$ java KicadModuleToGEDA -k Transmogrify/LED-DISPLAY-MAGNIFY-TRANSLATE-EXAMPLE.mod

which produces four files

	led-MSA5XXX-first.fp
	led-MSA5XXX-second.fp
	led-MSA5XXX-third.fp
	led-MSA5XXX-fourth.fp

in the ./Converted directory. The four modified files can be combined in a text
editor to produce a final merged footprint.

As previously discussed, the device outlines need to be altered, and the pins
altered, but the main goal of being able to translate and magnify complex
silkscreen artwork quite simply and quickly has been demonstrated.

