/**
 * 
 */
package edu.uccs.ecgs.play2;

import java.util.logging.*;
import javax.swing.JTextArea;

/**
 */
public class TextAreaHandler extends Handler {

  private JTextArea textArea;

  /**
   * 
   */
  public TextAreaHandler(JTextArea textArea) {
    super();
    this.textArea = textArea;

    Formatter formatter = new Formatter() {
      @Override
      public String format(LogRecord record) {
        return record.getMessage() + "\n";
      }
    };

    setFormatter(formatter);
  }

  /* (non-Javadoc)
   * @see java.util.logging.Handler#close()
   */
  @Override
  public void close() throws SecurityException
  {
  }

  /* (non-Javadoc)
   * @see java.util.logging.Handler#flush()
   */
  @Override
  public void flush()
  {
  }

  /* (non-Javadoc)
   * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
   */
  @Override
  public void publish(LogRecord record)
  {
    textArea.append(getFormatter().format(record));
  }  
}
