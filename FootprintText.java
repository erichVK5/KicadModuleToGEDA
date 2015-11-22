// FootprintText.java v1.0
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
//    FootprintText Copyright (C) 2015 Erich S. Heinzle a1039181@gmail.com

//
//  A utility for turning text strings into silkscreen line elements which can
//  be added to footprints for labelling purposes.
//  v1.0 of the utility uses the free Hershey Sans 1 Stroke Font and outputs
//  0.01mil (imperial, square bracketed) units. 
//

import java.io.*;
//import java.util.PrintWriter;

public class FootprintText extends FootprintElementArchetype {

  //    String output = "";
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

  String displayedTextField = "DefaultText-SpaceForRent";
  // default text to convert if nothing supplied


  // using chars 32 -> 126 = 95 in total

  double magnificationRatio = 1.0;      // default value of 1.0 yields default sized font text in gEDA PCB

  long yLayoutOffsetNm = 0;    // these are used to position the text relative to the module or layout centroid
  long xLayoutOffsetNm = 0;

  boolean metricFlag = false;  // not really needed for text, if we output silk strokes in decimil format regardless

  long kicadTextHeightNm = 0;
  long kicadTextWidthNm = 1320000;
  //
  // 1786000 -> ?70.37mil -> 67.3 actual
  // default value of 1.829mm for testing = 70.6mil, or 
  // 1327000 = 83.7mil

  public void FootprintText(long offsetX, long offsetY) {
    // (x,y) position relative to footprint or layout centroid
    xLayoutOffsetNm = offsetX;
    yLayoutOffsetNm = offsetY;
  }

  public void FootprintText()
  {
    ;
  }
  
  public String toString()
  {
    return kicadTextDescriptor;
  }



  public String generateGEDAelement(long offsetX, long offsetY, float magnificationRatio) {

    xLayoutOffsetNm = offsetX + xCoordNm; //try this
    // x position relative to footprint or layout centroid
    yLayoutOffsetNm = offsetY + yCoordNm;
    // y position relative to footprint or layout centroid

    double footprintMagnificationRatio = magnificationRatio;

    HersheySansFontClass hershey = new HersheySansFontClass();

    long xOffsetCentimil = 0;            
    // this is used to increment the x position of
    // the text character by character
    long yOffsetCentimil = 0;           
    // this is used to increment/set the y position
    // of the text character by character

    long textCentreOffsetXcentimil = 0;
    // Kicad specifies the x,y location of the CENTRE of the text

    hershey.setKicadMWidthNm(kicadTextWidthNm);

    long textCentreOffsetYcentimil = hershey.yCentredOffset();

    long textWidthCentimil = 0;
    String displayedElements  = "";

    // we need to figure out the width of the rendered string
    // we start by summing the widths of the individuals chars
    // remembering that hershey.width returns ((maxX-minX) + kerning)
    for (int i = 0; i < displayedTextField.length(); i++) {
      textWidthCentimil += hershey.width((int)(displayedTextField.charAt(i)));
    }
    // now we subtract the final kerning, add thickness, and 
    // account for the + 1000 offset of a gEDA font symbol
    // along the x-axis 
    textWidthCentimil = textWidthCentimil -
        hershey.kerning(displayedTextField.charAt(displayedTextField.length()-1))
        // lopped off end kerning to get final width
        + (long)(1000*hershey.fontMagnificationRatio)
        // accounted for final char being an extra +1000 along x axis
        + hershey.thickness((int)(displayedTextField.charAt(displayedTextField.length()-1))); 
        // and accounted for thickness of drawn lines

    // we now divide the string width by two to get the offset
    // needed to centre the rendered string
    textCentreOffsetXcentimil = (long)(textWidthCentimil / 2.0);

    // we now apply the overall x offset for the 
    // entire text string, relative to the footprint/module
    xOffsetCentimil = (long)((xLayoutOffsetNm/254 - textCentreOffsetXcentimil) * footprintMagnificationRatio);

    // we now apply the overall y offset for the entire
    // text string, relative to the footprint/module
    //    yOffsetCentimil = (long)((hershey.yCentredOffset() + yLayoutOffsetNm/254) * footprintMagnificationRatio);
    // we can call the auto - y - centring method of
    // the hersheyFontclass and let it do the work

    yOffsetCentimil = (long)((yLayoutOffsetNm/254) * footprintMagnificationRatio);
    
    for (int i = 0; i < displayedTextField.length(); i++){
      displayedElements = displayedElements +
          hershey.drawYCentredChar((int)(displayedTextField.charAt(i)), xOffsetCentimil, yOffsetCentimil, footprintMagnificationRatio, true);
      xOffsetCentimil += 
          (long)(hershey.width((int)(displayedTextField.charAt(i)))*footprintMagnificationRatio);
    }
    return displayedElements;
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
        String rawString = tokens[10];
        // we only want what is inside double quotes
        displayedTextField = rawString.substring(rawString.indexOf('"') + 1, rawString.lastIndexOf('"'));
        
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
  

  //  public void populateElement(String moduleDefinition, boolean metric) {
  //  kicadTextDescriptor = moduleDefinition;
        // for testing
  //  workingText = kicadTextDescriptor; //"Sample Text";
  //}

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
