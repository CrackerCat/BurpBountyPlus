package burpbounty;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ���ô����࣬������ʾ���ô��ڣ���������Ϣ
 */
public class ConfigDlg extends JDialog {
    private final JPanel mainPanel = new JPanel();
    private final JPanel topPanel =  new JPanel();
    private final JPanel centerPanel = new JPanel();
    private final JPanel bottomPanel = new JPanel();;
    private final JLabel Title = new JLabel("Ingored Param:");
    private final JLabel tagsNumber = new JLabel("Thread Pool Numbers:");
    private final JSpinner spNum = new JSpinner(new SpinnerNumberModel(20, 1, 100, 5));


    private final JTextArea IgnoreParamText  =new JTextArea(10,50);
    private final JButton btSave = new JButton("Save");
    private final JButton btCancel = new JButton("Cancel");


    public ConfigDlg(){
        initGUI();
        initEvent();
        initValue();
        this.setTitle("BurpBountyPlus Config");
    }


    /**
     * ��ʼ��UI
     */
    private void initGUI(){

        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(tagsNumber);
        topPanel.add(spNum);

        IgnoreParamText.setLineWrap(true);// �����Զ����й���
        IgnoreParamText.setWrapStyleWord(true);// ������в����ֹ���
        //IgnoreParamText.setBackground(Color.PINK);
        IgnoreParamText.setFont(new Font( "Dialog",Font.BOLD, 13));
        IgnoreParamText.setText(Config.get_ignore_param());
        spNum.setValue(Config.get_threadpool_threadnum());


        centerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        Box box = Box.createVerticalBox();
        box.add(Title);
        box.add(new JScrollPane(IgnoreParamText));
        centerPanel.add(box);

        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(btSave);
        bottomPanel.add(btCancel);

        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(topPanel,BorderLayout.NORTH);
        mainPanel.add(centerPanel,BorderLayout.CENTER);
        mainPanel.add(bottomPanel,BorderLayout.SOUTH);

        this.setModal(true);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.add(mainPanel);
        //ʹ���ô����Զ���Ӧ�ؼ���С����ֹ���ֿؼ��޷���ʾ
        this.pack();
        //������ʾ���ô���
        Dimension screensize=Toolkit.getDefaultToolkit().getScreenSize();
        this.setBounds(screensize.width/2-this.getWidth()/2,screensize.height/2-this.getHeight()/2,this.getWidth(),this.getHeight());
    }


    /**
     * ��ʼ���¼�
     */
    private void initEvent(){

        btCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigDlg.this.dispose();
            }
        });

        btSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Integer ThreadNum = (Integer)spNum.getValue();
                String ignoreParm = IgnoreParamText.getText();
                Config.set_ignore_param(ignoreParm);
                Config.set_threadpool_threadnum(ThreadNum);
                ConfigDlg.this.dispose();
            }
        });
    }


    /**
     * Ϊ�ؼ���ֵ
     */
    public void initValue(){

    }
}