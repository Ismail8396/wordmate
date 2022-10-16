package gui.utils.icons;

import com.formdev.flatlaf.FlatLightLaf;
import utils.Constants;

import javax.swing.*;

public class IconManager {

    private static IconManager single_instance = null;

    public static IconManager getInstance(){
        if(single_instance == null)
            single_instance = new IconManager();

        return single_instance;
    }

    public ImageIcon getIcon(ApplicationIcons icon){
        String fileName = null;

        if(icon.equals(ApplicationIcons.ICON_MENU_THEME))
            fileName = Constants.MENU_THEME_ICON;
        else if(icon.equals(ApplicationIcons.ICON_MENU_OPTIONS))
            fileName = Constants.MENU_OPTIONS_ICON;
        else if (icon.equals(ApplicationIcons.ICON_MENU_CLOSE))
            fileName = Constants.MENU_CLOSE_ICON;
        else if(icon.equals(ApplicationIcons.ICON_SWAP))
            fileName = Constants.SWAP_ICON;
        else if(icon.equals(ApplicationIcons.ICON_PLAY))
            fileName = Constants.PLAY_ICON;
        else if(icon.equals(ApplicationIcons.ICON_STOP))
            fileName = Constants.STOP_ICON;
        else
            System.err.printf("%s: Unexpected icon request", this.getClass().getName());

        // dark or light theme
        if(UIManager.getLookAndFeel() instanceof FlatLightLaf){
            fileName += Constants.LIGHT_THEME_SUFFIX;
        }else{
            fileName += Constants.DARK_THEME_SUFFIX;
        }

        fileName += Constants.IMAGE_ICON_EXTENSION;

        return new ImageIcon(fileName,fileName);
    }
}
