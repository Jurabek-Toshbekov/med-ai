package uz.sdg.sos.component;

import org.springframework.core.io.ClassPathResource;

import javax.swing.*;
import java.util.Properties;

public class InitConfig {
     final static String deleteWord="123456";

    public static boolean isStart() {
        Properties props = new Properties();
        try {
            props.load(new ClassPathResource("/application.yml").getInputStream());
            if (props.getProperty("ddl-auto").equals("update")) {
                return true;
            } else {
                String confirm = JOptionPane.showInputDialog("Ma'lumotlarni o'chirib yuborma! Keyin bilmay qoldim dema! Agar rostdan ham o'chirmoqchi bo'lsang. O'chirish kodi : "+deleteWord);
                if (confirm != null && confirm.equals(deleteWord)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
