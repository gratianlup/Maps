// Copyright (c) 2010 Gratian Lup. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following
// disclaimer in the documentation and/or other materials provided
// with the distribution.
//
// * The name "JavaMaps" must not be used to endorse or promote
// products derived from this software without prior written permission.
//
// * Products derived from this software may not be called "JavaMaps" nor
// may "JavaMaps" appear in their names without prior written
// permission of the author.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package Rendering.Utils;
import javax.swing.*;
import java.awt.*;

/** A few utilities that simplify testing of windows in Swing.
 *  1998 Marty Hall, http://www.apl.jhu.edu/~hall/java/
 */

public class WindowUtilities {

  /** A simplified way to see a JPanel or other Container.
   *  Pops up a JFrame with specified Container as the content pane.
   */

  public static JFrame openInJFrame(JFrame frame, Container content,
                                    int width,
                                    int height,
                                    String title,
                                    Color bgColor) {
    frame.setBackground(bgColor);
    content.setBackground(bgColor);
    frame.setSize(width, height);
    frame.setContentPane(content);
    frame.setVisible(true);

    return(frame);
  }

  /** Uses Color.white as the background color. */

  public static JFrame openInJFrame(JFrame frame, Container content,
                                    int width,
                                    int height,
                                    String title) {
    return(openInJFrame(frame, content, width, height, title, Color.white));
  }

  /** Uses Color.white as the background color, and the
   *  name of the Container's class as the JFrame title.
   */

  public static JFrame openInJFrame(JFrame frame, Container content,
                                    int width,
                                    int height) {
    return(openInJFrame(frame, content, width, height,
                        content.getClass().getName(),
                        Color.white));
  }
}
