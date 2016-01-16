// KicadModuleToGEDA - a utility for turning kicad modules to gEDA PCB footprints
// Text.java v1.0
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

public class Text extends FootprintElementArchetype
{

    String output = "";
    String kicadTextDescriptor = "";

    long xCoordNm = 0;
    long yCoordNm = 0;
    long kicadMheightNm = 0;
    long kicadMwidthNm = 0;
    long kicadRotation = 0;
    long defaultGEDAlineThickness = 1000; // this is 10 mil in 0.01 mil units
    long defaultLineThicknessNm = 254000; // which is 254000 nanometres
			// which is 254 microns, which is 0.254 mm
			// which is 0.01 inches, which is 10 mil = 10 thou
    long kicadLineThicknessNm = defaultLineThicknessNm;
    boolean textVisibility = true;
    int kicadBottomSilkLayer = 20;
    int kicadTopSilkLayer = 21;
    int kicadBottomCopperLayer = 0;
    int kicadTopCopperLayer = 15;
    int textLayer = 21;
    String displayedTextField = "";

    public void Text()
    {
        output = "# Hmm, the no arg constructor for the text object didn't do much";
    }

    public String toString()
    {
        return kicadTextDescriptor;
    }

    public void populateElement(String arg, boolean metric)
    {
        kicadTextDescriptor = arg;
        String[] tokens = kicadTextDescriptor.split(" ");
        float parsedValue = 0;        

        if (tokens[0].startsWith("T"))
	{
            parsedValue = Float.parseFloat(tokens[1]);
            xCoordNm = convertToNanometres(parsedValue, metric); 
            parsedValue = Float.parseFloat(tokens[2]);
            yCoordNm = convertToNanometres(parsedValue, metric);
            parsedValue = Float.parseFloat(tokens[3]);
            kicadMheightNm = convertToNanometres(parsedValue, metric);
            parsedValue = Float.parseFloat(tokens[4]);
            kicadMwidthNm = convertToNanometres(parsedValue, metric);
            kicadRotation = Integer.parseInt(tokens[5]);
            parsedValue = Float.parseFloat(tokens[6]);
            kicadLineThicknessNm = convertToNanometres(parsedValue, metric);
            if (tokens[8].startsWith("I"))
                {
                    textVisibility = false;
                }
            else if (tokens[8].startsWith("V"))
                {
                    textVisibility = true;
                }
            textLayer = Integer.parseInt(tokens[9]);
            displayedTextField = tokens[10];
            
        }
        else if (tokens[0].startsWith("fbtext"))
        { // s-files seem to have limited support for multiple text fields
            for (int counter = 1; counter < tokens.length; counter++)
                {
                    if (tokens[counter].startsWith("reference"))
                        {
                            displayedTextField = tokens[counter + 1];
                            counter++;
                        }
                    else if (tokens[counter].startsWith("value"))
                        {
                            displayedTextField = tokens[counter + 1];
                            counter++;
                        }
                    else if (tokens[counter].startsWith("at"))
                        {
                            counter++;
                            parsedValue = Float.parseFloat(tokens[counter]);
                            xCoordNm = convertToNanometres(parsedValue, metric);
                            counter++;
                            parsedValue = Float.parseFloat(tokens[counter]);
                            yCoordNm = convertToNanometres(parsedValue, metric);                            
                        }
                    else if (tokens[counter].startsWith("size"))
                        {
                            counter++;
                            parsedValue = Float.parseFloat(tokens[counter]);
                            kicadMheightNm = convertToNanometres(parsedValue, metric);
                            counter++;
                            parsedValue = Float.parseFloat(tokens[counter]);
                            kicadMwidthNm = convertToNanometres(parsedValue, metric);                            
                        }
                    else if (tokens[counter].startsWith("thickness"))
                        {
                            counter++;
                            parsedValue = Float.parseFloat(tokens[counter]);
                            kicadLineThicknessNm = convertToNanometres(parsedValue, metric);
                        }
                    else if (tokens[counter].startsWith("layer"))
                        {
                            counter++;
                            if (tokens[counter].startsWith("F.Silks"))
                                {
                                    textLayer = kicadTopSilkLayer;
                                }
                            else if (tokens[counter].startsWith("F.Silks"))
                                {
                                    textLayer = kicadBottomSilkLayer;
                                }
                            // we could add additional layer suport here if keen
                        }
                    else if (tokens[counter].startsWith("hide"))
                        {
                            textVisibility = false;
                        }

                }
            
        }
    }


    private long convertToNanometres(float rawValue, boolean metricSystem)
    {
        if (metricSystem)//  metric = the newer legacy format with mm instead of decimil = 0.1 mil units
            {
                return (long)(rawValue * 10000000); 
            }
        else // this implies the input is in Kicad legacy decimil = 0.1mil units
            {
                return (long)(2540 * rawValue); // a 0.1 mil unit = 2540 nm
            }
    }
}
