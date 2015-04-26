package linefit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A Class that extends JDialog that allows the user to choose custom colors to use for the lines
 * @author Keith Rice
 * @version	1.0
 * @since 	0.98.1
 *
 */
class CustomColorMenu extends JFrame
{	
	/** The Serial Version UID so that we know what version it is when we are using it.
	 * See http://docs.oracle.com/javase/7/docs/api/java/io/Serializable.html for full 
	 * discussion on its uses and purpose */
	private static final long serialVersionUID = 42L;
	/** The Color that was chosen before this selector was started */
	private Color startColor;
	/** The DataSet that this Color selector goes with */
	private DataSet goesWith;
	/** The JColorChooser that goes with this menu which is what actually allows the user to specify the color to use */
	private JColorChooser customColorChooser;
	
	/**
	 * Creates a new Color Selector that is paired with the passed in dataset
	 * @param dataSetThisGoesWith The DataSet that this CustomColorMenu is linked with
	 */
	CustomColorMenu(DataSet dataSetThisGoesWith)
	{
		goesWith = dataSetThisGoesWith;
		
		setSize(600, 400);
		//make it so you cant do other things setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
		setLayout(new BorderLayout());
		
		//Makes whereToPut null before we close so we know not to do anything
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		//add our listener to override the default close button action
		addWindowListener(new WindowClosingOverrideAdapter());
		
		//Create the custom color chooser and set it to the reserved color(because we only create one once) and add the listener and then add it to our frame
		customColorChooser = new JColorChooser();
		customColorChooser.getSelectionModel().setSelectedColor(ColorBoxRenderer.RESERVED_FOR_CUSTOM_COLOR);
		customColorChooser.getSelectionModel().addChangeListener(new ColorChangedListener());
		try 
		{
			removeTransparencySlider(customColorChooser);
		} 
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) 
		{
			//we failed to remove the sliders but its no big deal - just keep going
		}
		add(customColorChooser, BorderLayout.CENTER);
		
		JPanel buttonRow = new JPanel();
		
		JButton impBut = new JButton("Select");
		impBut.addActionListener(new SelectButtonListener());
		buttonRow.add(impBut, BorderLayout.WEST);

		JButton cancBut = new JButton("Cancel");
	    cancBut.addActionListener(new CancelButtonListener());
		buttonRow.add(cancBut, BorderLayout.EAST);
		add(buttonRow, BorderLayout.SOUTH);

		initialize();
	}

	/**
	 * Initializes this CustomColorMenu by setting the start color and making it visible
	 */
	void initialize()
	{
		startColor = goesWith.getColor();
		goesWith.setColor(customColorChooser.getSelectionModel().getSelectedColor());
		setVisible(true);
	}
	
	/**
	 * Reverts the DataSet that this color selector goes with to the color it was when the selector was first opened
	 */
	private void undoColorChangeAndExitCustomColorMenu()
	{
		goesWith.setColor(startColor);
		exitCustomColorMenu();
	}

	/**
	 * Hides/closes the CustomColorMenu
	 */
	private void exitCustomColorMenu()
	{
		this.setVisible(false);
	}

	/** A function that removes the transparency slider from the JColorChooser taken from stack overflow question: http://stackoverflow.com/questions/12026767/java-7-jcolorchooser-disable-transparency-slider
	 * Post Author: user3455283
	 * @param jColorChooserToRemoveFrom The JColorChooser to remove the transparency slider from
	 * @throws NoSuchFieldException throws this error if it cannot find the transparency slider to remove
	 * @throws SecurityException throws this error if it encounters a security problem when removing the slider
	 * @throws IllegalArgumentException throws this error if an illegal argument is encounter when trying to remove the transparency slider
	 * @throws IllegalAccessException throws this error if when removing the transparency slider we do not have access to remove the slider
	 */
	private static void removeTransparencySlider(JColorChooser jColorChooserToRemoveFrom) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException 
	{
	    AbstractColorChooserPanel[] colorPanels = jColorChooserToRemoveFrom.getChooserPanels();
	    for (int i = 1; i < colorPanels.length; i++) 
	    {
	        AbstractColorChooserPanel cp = colorPanels[i];

	        Field f = cp.getClass().getDeclaredField("panel");
	        f.setAccessible(true);

	        Object colorPanel = f.get(cp);
	        Field f2 = colorPanel.getClass().getDeclaredField("spinners");
	        f2.setAccessible(true);
	        Object spinners = f2.get(colorPanel);

	        Object transpSlispinner = Array.get(spinners, 3);
	        if (i == colorPanels.length - 1) 
	        {
	            transpSlispinner = Array.get(spinners, 4);
	        }
	        Field f3 = transpSlispinner.getClass().getDeclaredField("slider");
	        f3.setAccessible(true);
	        JSlider slider = (JSlider) f3.get(transpSlispinner);
	        slider.setEnabled(false);
	        slider.setVisible(false);
	        Field f4 = transpSlispinner.getClass().getDeclaredField("spinner");
	        f4.setAccessible(true);
	        JSpinner spinner = (JSpinner) f4.get(transpSlispinner);
	        spinner.setEnabled(false);
	        spinner.setVisible(false);

	        Field f5 = transpSlispinner.getClass().getDeclaredField("label");
	        f5.setAccessible(true);
	        JLabel label = (JLabel) f5.get(transpSlispinner);
	        label.setVisible(false);
	    }
	}


	//private classes
	/**
	 * A Listener class that is used to change the currently selected color for the DataSet to the selected one
	 * 
	 * @author Keith Rice
	 * @version	1.0
	 * @since 	0.98.0
	 */
	private class ColorChangedListener implements ChangeListener
	{
		@Override
		public void stateChanged(ChangeEvent chooserSource) 
		{
			ColorSelectionModel source = (ColorSelectionModel)chooserSource.getSource();
			goesWith.setColor(source.getSelectedColor());
		}
	}
	
	/**
	 * A Listener class that is used to see if the Select button has been clicked and then handle its action
	 * 
	 * @author	Keith Rice
	 * @version	1.0
	 * @since 	0.98.0
	 */
	private class SelectButtonListener implements ActionListener
	{
		/** 
		 * The action that occurs when the Select Button was clicked that saves the selected color to return
		 */
	    public void actionPerformed(ActionEvent e) 
	    {
	        //The color is already changed so just close us
	    	exitCustomColorMenu();
	    }
	}
	
	/**
	 * A Listener class that listens for the cancel button or the close button to be pressed and then handles what is done
	 * 
	 * @author	Keith Rice
	 * @version	1.0
	 * @since 	0.98.0
	 */
	private class CancelButtonListener implements ActionListener
	{
		/** The action that is performed when the user exits out of the import and that makes sure the return
		 * Color is null so we know it has been canceled
		 */
	    public void actionPerformed(ActionEvent e) 
	    {
	    	undoColorChangeAndExitCustomColorMenu();
	    }
	}

	/** 
	 * A private class that overrides WindowAdapter that allows us to override the default function when the close button is pressed 
	 * @author	Keith Rice
	 * @version	1.0
	 * @since 	0.98.0
	 */
	private class WindowClosingOverrideAdapter extends WindowAdapter
	{
		public void windowClosing(WindowEvent e) 
		{
			undoColorChangeAndExitCustomColorMenu();
		}
	}	
}