package source;

import java.awt.EventQueue;
import javax.swing.JFrame;

// main method
public class CapBoy extends JFrame {

	private static final long serialVersionUID = 1130155880849515498L;

	public CapBoy() {
        
        initUI();
    }
	
    // load the Maze class && set size of the window
    private void initUI() {
        
        add(new aMAZE());
        
        setTitle("capBoy -2.0");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 430);
        setLocationRelativeTo(null);
    }
    
    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {
            CapBoy ex = new CapBoy();
            ex.setVisible(true);
        });
    }
}