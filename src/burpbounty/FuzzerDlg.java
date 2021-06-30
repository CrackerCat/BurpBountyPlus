package burpbounty;

import burp.IBurpExtenderCallbacks;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTabbedPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ���ô����࣬������ʾ���ô��ڣ���������Ϣ
 */
public class FuzzerDlg extends JFrame {
    private final JPanel mainPanel = new JPanel();
    private final JPanel centerPanel = new JPanel();
    private final JPanel topPanel = new JPanel();;
    public  JTabbedPane TablePanel = new JTabbedPane();
    private Tags tagui;
    private IBurpExtenderCallbacks callbacks;
    public ThreadPoolExecutor executor;

    private final JTextArea IgnoreParamText  =new JTextArea(10,50);
    private final JButton btSave = new JButton("Stop Fuzz");
    private final JButton btCancel = new JButton("Cancel");


    public FuzzerDlg(IBurpExtenderCallbacks callbacks){
        this.callbacks = callbacks;
        initGUI();
        initEvent();
        initValue();
        this.setTitle("BurpBountyPlus Fuzzer");
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Config.get_threadpool_threadnum());
    }
    /**
     * ��ʼ��UI
     */
    public Tags get_dlg_tags(){
        return this.tagui;
    }
    private void initGUI(){
        centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        tagui =new Tags(callbacks, "Fuzzer", TablePanel);
        centerPanel.add(TablePanel);

        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        topPanel.add(btSave);

        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(topPanel,BorderLayout.NORTH);
        mainPanel.add(TablePanel,BorderLayout.CENTER);


        Dimension screensize=Toolkit.getDefaultToolkit().getScreenSize();
        //this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(screensize.width/2,screensize.height-20);
        this.setVisible(true);
        this.add(mainPanel);
        this.addWindowListener(new WindowAdapter() {
                              @Override
                              public void windowClosing(WindowEvent e) {
                                  super.windowClosing(e);
                                  //do something
                                  try {
                                      executor.shutdownNow();

                                  } catch (Exception ex) {
                                      callbacks.printError(ex.getMessage());
                                  }
                              }});

        //ʹ���ô����Զ���Ӧ�ؼ���С����ֹ���ֿؼ��޷���ʾ
        //this.pack();
        //������ʾ���ô���
        this.setBounds(screensize.width/2-this.getWidth()/2,screensize.height/2-this.getHeight()/2,this.getWidth(),this.getHeight());
    }


    /**
     * ��ʼ���¼�
     */
    private void initEvent(){

        btCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //FuzzerDlg.this.dispose();
            }
        });

        btSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    executor.shutdownNow();

                } catch (Exception ex) {
                    callbacks.printError(ex.getMessage());
                }
            }
        });
    }


    /**
     * Ϊ�ؼ���ֵ
     */
    public void initValue(){

    }
}