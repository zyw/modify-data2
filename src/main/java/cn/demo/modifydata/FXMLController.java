package cn.demo.modifydata;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.demo.modifydata.util.ConfigFileUtils;
import cn.demo.modifydata.util.EnumStopFlag;
import cn.demo.modifydata.util.MD5Util;
import cn.demo.modifydata.util.NetworkUtil;
import com.google.common.collect.*;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class FXMLController implements Initializable {

    private Stage stage;

    @FXML
    private TextField fileName;

    @FXML
    private ChoiceBox hourCB;

    @FXML
    private ChoiceBox minuteCB;

    @FXML
    private TextField input1;

    @FXML
    private TextField input2;

    @FXML
    private TextField secretKey;

    private static final FileChooser FILE_CHOOSER = new FileChooser();

    private static final String DATETIME_PATTERN_STR = "\\d*-\\d*-\\d*\\s*\\d*:\\d*\\s*-\\s*\\d*:\\d*";
    private static final Pattern DATETIME_PATTERN = Pattern.compile("\\d*-\\d*-\\d*\\s*\\d*:\\d*\\s*-\\s*\\d*:\\d*");

    /**
     * 匹配文件名称
     */
    private static final String FILENAME_PATTERN_STR = "\\w*.dat";
    private static final Pattern FILENAME_PATTERN = Pattern.compile(FILENAME_PATTERN_STR);

    /**
     * 时间正则表达式
     */
    private static final String TIME_PATTERN_STR = "\\d*:\\d*:\\d*";
    private static final Pattern TIME_PATTERN = Pattern.compile(TIME_PATTERN_STR);

    /**
     * 标识正则表达式
     */
    private static final String R_FLAG_M_STR = "R[a-z]?\\s*\\d.\\d* m";
    private static final Pattern R_FLAG_M_PATTERN = Pattern.compile(R_FLAG_M_STR);
    private static final Pattern R_FLAG_M_PATTERN_2 = Pattern.compile("\\d.\\d*");

    /**
     * 标识正则表达式
     */
    private static final String HD_DISTANCE_M_STR = "HD\\s*\\d*.\\d* m";
    private static final Pattern HD_DISTANCE_M_PATTERN = Pattern.compile(HD_DISTANCE_M_STR);
    private static final Pattern HD_DISTANCE_M_PATTERN_2 = Pattern.compile("\\d?\\d?\\d.\\d*");

    /**
     * 标量1
     */
    private static final Integer SCALAR1_START = 45;
    private static final Integer SCALAR1_END = 48;

    private static final String Z_FLAG = "Z";
    private static final Integer Z_FLAG_START = 95;
    private static final Integer Z_FLAG_END = 96;

    private static final String RB_FLAG = "Rb";
    private static final String RF_FLAG = "Rf";
    private static final String RZ_FLAG = "Rz";
    private static final String DB_FLAG = "Db";

    private static final String SH_FLAG = "Sh";

    private static final Integer BFZ_FLAG_START = 49;
    private static final Integer BFZ_FLAG_END = 51;

    private static final String DZ_FLAG = "dz";
    private static final String DF_FLAG = "Df";
    private static final Integer ZF_FLAG_START = 72;
    private static final Integer ZF_FLAG_END = 74;

    private static final Integer HD_INDEX = 75;

    private static final Integer COLUMN_LEN = 119;
    private static final Double TOTAL = 10.0;

    /**
     * 不能超线
     */
    private static final Double RBFZ_MAX = 2.0;
    private static final Double HD_MAX = 30.0;

    private static final Random RANDOM = new Random();

    /**
     * 停止标识
     */
    private static EnumStopFlag stop_flag = EnumStopFlag.NORMAL;

    private static final String CANCEL_SKIP = "#####";

    private List<Double> Z_NUM_LIST = null;
    private List<Double> RB_NUM_LIST = null;
    private List<Double> RF_NUM_LIST = null;
    /**
     * RF结果数列表
     */
    private List<Double> RFR_NUM_LIST = null;

    private List<Double> RBH_NUM_LIST = null;
    private List<Double> RFH_NUM_LIST = null;

    private ConfigModel configModel;

    private List<Integer> rbfList = null;
    private List<Integer> rzList = null;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        try {

            String name = fileName.getText();

            if(StringUtils.isEmpty(name)) {
                Alert alert = new Alert(Alert.AlertType.ERROR,"请输入文件名称！");
                alert.showAndWait();
                return;
            }
            Object hObj = this.hourCB.getValue();
            if(hObj == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR,"请选择小时！");
                alert.showAndWait();
                return;
            }
            String hour = String.valueOf(hObj);

            Object mObj = this.minuteCB.getValue();
            if(mObj == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR,"请选择分钟！");
                alert.showAndWait();
                return;
            }

            String minute = String.valueOf(mObj);

            String flagStr = input1.getText();
            if(StringUtils.isEmpty(flagStr)) {
                Alert alert = new Alert(Alert.AlertType.ERROR,"请输入标记！");
                alert.showAndWait();
                return;
            }
            Double flagVal = Double.valueOf(flagStr);

            String text = input2.getText();
            if(StringUtils.isEmpty(text)) {
                Alert alert = new Alert(Alert.AlertType.ERROR,"请输入距离！");
                alert.showAndWait();
                return;
            }
            Double distance = Double.valueOf(text);

            File file = FILE_CHOOSER.showOpenDialog(stage);

            if(file != null) {

                File outFile = new File(name + ".DAT");

                List<String> result = new ArrayList<>();

                List<String> contents = FileUtils.readLines(file, "GBK");

                //全局数据初始化
                initGlobalList();
                stop_flag = EnumStopFlag.NORMAL;

                contents.stream()
                        .map(content -> handleDateTime(content,hour,minute))
                        .map(content -> handleFileName(content, name))
                        /*对时间进行处理*/
                        .map((content) -> handleTime(content,hour,minute))
                        /*对标识进行处理*/
                        .map((content) -> handleFlag(content, flagVal))
                        /*对距离进行处理*/
                        .map((content) -> handleDistance(content,distance))
                        /*给标量+1*/
                        .map(this::scalarPlusOne)
                        .map(this::calculation)
                        .forEachOrdered(result::add);

                switch (stop_flag) {
                    case NORMAL:
                        //写出文件
                        FileUtils.writeLines(outFile, "GBK", result, true);
                        new Alert(Alert.AlertType.INFORMATION,"成功！").show();
                        break;
                    case RB_RF_RZ:
                        new Alert(Alert.AlertType.WARNING,"Rb,Rf,Rz列超线了！").show();
                        break;
                    case RB_RF_RZ_FS:
                        new Alert(Alert.AlertType.WARNING,"Rb,Rf,Rz为负数了！").show();
                        break;
                    case HD:
                        new Alert(Alert.AlertType.WARNING,"HD列超线了！").show();
                        break;
                    default:
                        new Alert(Alert.AlertType.WARNING,"未知原因未生成文件！").show();
                }
            }
        } catch (IOException ex) {
            String message = Optional.ofNullable(ex.getMessage()).orElse(ex.getCause().getMessage());
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, message, ex);
            new Alert(Alert.AlertType.ERROR,message).showAndWait();
        } catch (Exception e) {
            String message = Optional.ofNullable(e.getMessage()).orElse(e.getCause().getMessage());
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, message, e);
            new Alert(Alert.AlertType.ERROR,message).showAndWait();
        }
    }

    private void initGlobalList() {
        Z_NUM_LIST = Lists.newArrayList();
        RB_NUM_LIST = Lists.newArrayList();
        RF_NUM_LIST = Lists.newArrayList();
        RFR_NUM_LIST = Lists.newArrayList();

        RBH_NUM_LIST = Lists.newArrayList();
        RFH_NUM_LIST = Lists.newArrayList();
    }

    private String calculation(String content) {
        if(stop_flag != EnumStopFlag.NORMAL) {
            return content;
        }
        if(content.length() < COLUMN_LEN) {
            return content;
        }
        if(StringUtils.contains(content,CANCEL_SKIP)) {
            return content;
        }
        String bfz = content.substring(BFZ_FLAG_START,BFZ_FLAG_END);
        String z = content.substring(Z_FLAG_START, Z_FLAG_END);
        if(StringUtils.equals(z,Z_FLAG) && StringUtils.isBlank(bfz)) {
            String zNumStr = content.substring(Z_FLAG_END, Z_FLAG_END + 16).trim();
            Double zNum = Double.valueOf(zNumStr);
            if(zNum.doubleValue() == TOTAL.doubleValue()) {
                Z_NUM_LIST.add(zNum);
            } else {
                Double aDouble = calcZ(lastItem(Z_NUM_LIST), RB_NUM_LIST, RF_NUM_LIST);
                RFR_NUM_LIST.add(average(RF_NUM_LIST));
                RB_NUM_LIST = Lists.newArrayList();
                RF_NUM_LIST = Lists.newArrayList();
                Z_NUM_LIST.add(aDouble);
                String s = formatDouble(aDouble, "##0.00000");
                return content.substring(0, Z_FLAG_END) + addSpace(16-s.length()) + s + content.substring(Z_FLAG_END + 16, content.length());
            }
        }
        if(StringUtils.equals(bfz,RB_FLAG)) {
            String rbNumStr = content.substring(BFZ_FLAG_END, BFZ_FLAG_END + 15).trim();
            RBH_NUM_LIST.add(Double.valueOf(content.substring(HD_INDEX, HD_INDEX + 15).trim()));
            RB_NUM_LIST.add(Double.valueOf(rbNumStr));
        }
        if(StringUtils.equals(bfz,RF_FLAG)) {
            String rfNumStr = content.substring(BFZ_FLAG_END, BFZ_FLAG_END + 15).trim();
            RFH_NUM_LIST.add(Double.valueOf(content.substring(HD_INDEX, HD_INDEX + 15).trim()));
            RF_NUM_LIST.add(Double.valueOf(rfNumStr));
        }
        if(StringUtils.equals(bfz,RZ_FLAG)) {
            String rzNumStr = content.substring(BFZ_FLAG_END, BFZ_FLAG_END + 15).trim();
            String rzz = formatDouble(lastItem(Z_NUM_LIST) + lastItem(RFR_NUM_LIST) - Double.valueOf(rzNumStr), "##0.00000");
            return content.substring(0, Z_FLAG_END) + addSpace(16-rzz.length()) + rzz + content.substring(Z_FLAG_END + 16, content.length());
        }

        if(StringUtils.equals(bfz,SH_FLAG)) {
            double shr = lastItem(Z_NUM_LIST) - 10;
            String sh = formatDouble(shr,"##0.00000");
            String s = content.substring(0, BFZ_FLAG_END) + addSpace(15 - sh.length()) + sh + content.substring(BFZ_FLAG_END + 15, content.length());
            String dz = formatDouble(Math.abs(shr), "##0.00000");
            return s.substring(0, ZF_FLAG_END) + addSpace(15 - dz.length()) + dz + s.substring(ZF_FLAG_END + 15, s.length());
        }

        if(StringUtils.equals(bfz,DB_FLAG)) {
            String db = formatDouble(sum(RBH_NUM_LIST) / 2, "##0.00");
            String df = formatDouble(sum(RFH_NUM_LIST) / 2, "##0.00");
            String lastZ = formatDouble(lastItem(Z_NUM_LIST), "##0.00000");
            String s = content.substring(0, BFZ_FLAG_END) + addSpace(15 - db.length()) + db + content.substring(BFZ_FLAG_END + 15, content.length());
            String s1 = s.substring(0, ZF_FLAG_END) + addSpace(15 - df.length()) + df + s.substring(ZF_FLAG_END + 15, s.length());
            return s1.substring(0, Z_FLAG_END) + addSpace(16-lastZ.length()) + lastZ + s1.substring(Z_FLAG_END + 16, s1.length());
        }
        return content;
    }

    /**
     * 给标量+1
     * @param content
     * @return
     */
    private String scalarPlusOne(String content) {
        if(stop_flag != EnumStopFlag.NORMAL) {
            return content;
        }
        if(content.length() < SCALAR1_END) {
            return content;
        }
        String scalar = content.substring(SCALAR1_START, SCALAR1_END);
        if(!StringUtils.isNumeric(scalar)) {
            return content;
        }
        int scalarResult = Integer.valueOf(scalar) + 1;
        return content.substring(0, SCALAR1_START) + scalarResult + content.substring(SCALAR1_END, content.length());
    }

    private String handleDateTime(String content,String hour,String minute) {
        Matcher matcher = DATETIME_PATTERN.matcher(content);
        if(matcher.find()) {
            String group = matcher.group(0);

            String[] split = StringUtils.split(group, " ");
            String[] split1 = StringUtils.split(split[1], ":");
            String[] split2 = StringUtils.split(split[3], ":");
            int endTime = Integer.valueOf(split2[0]) * 60 + Integer.valueOf(split2[1]);
            int startTime = Integer.valueOf(split1[0]) * 60 + Integer.valueOf(split1[1]);
            int chaTime = endTime - startTime;
            int resultTime = Integer.valueOf(hour) * 60 + Integer.valueOf(minute) + chaTime;

            int h = resultTime / 60;
            int m = resultTime % 60;

            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd"));
            group = group.replaceAll("\\d*-\\d*-\\d*",date)
                    .replaceAll("\\d*:\\d*\\s*-",hour+":"+minute+" -")
                    .replaceAll("-\\s*\\d*:\\d*","- " + repairZero(String.valueOf(h)) + ":" + repairZero(String.valueOf(m)));

            content = content.replaceAll(DATETIME_PATTERN_STR,group);
        }
        return content;
    }

    private String handleFileName(String content,String name) {
        Matcher matcher = FILENAME_PATTERN.matcher(content);
        String fullName = name+".bat";
        String regex = FILENAME_PATTERN_STR;
        if(matcher.find()) {
            String group = matcher.group(0);
            final String space = addSpace(Math.abs(group.length() - fullName.length()));
            if(group.length() > fullName.length()) {
                fullName = fullName + space;
            }

            if(group.length() < fullName.length()) {
                regex = regex + space;
            }
        }
        return content.replaceAll(regex, fullName);
    }


    private String prevTimeHour = "";
    private String prevTimeMin = "";
    private String inputPrevTimeHour = "";
    private String inputPrevTimeMin = "";

    /**
     * 处理时间
     * @param content
     * @return
     */
    private String handleTime(String content,String hour,String minute) {
        Matcher m1 = TIME_PATTERN.matcher(content);
        if(m1.find()) {
            String regex = TIME_PATTERN_STR;
            String traget = hour + ":" + minute;
            String findContent = m1.group(0);

            String[] timePart = StringUtils.split(findContent, ":");

            final String space = addSpace(Math.abs(traget.length() - findContent.length()));

            if(StringUtils.isEmpty(prevTimeHour)) {
                inputPrevTimeHour = hour;
                inputPrevTimeMin = minute;
                traget = traget + ":" + timePart[2];
            } else {
                int prevMin = Integer.valueOf(prevTimeHour) * 60 + Integer.valueOf(prevTimeMin);
                int nextMin = Integer.valueOf(timePart[0]) * 60 + Integer.valueOf(timePart[1]);
                int orgTime = nextMin - prevMin;

                int i = Integer.valueOf(inputPrevTimeHour) * 60 + Integer.valueOf(inputPrevTimeMin) + orgTime;
                int h = i / 60;
                int m = i % 60;

                traget = repairZero(String.valueOf(h)) + ":" + repairZero(String.valueOf(m)) + ":" + timePart[2];

                inputPrevTimeHour = String.valueOf(h);
                inputPrevTimeMin = String.valueOf(m);
            }

            prevTimeHour = timePart[0];
            prevTimeMin =  timePart[1];

            /*补充空格*/
            if(traget.length() > findContent.length()) {
                regex = regex + space;
            }
            if(traget.length() < findContent.length()) {
                traget = traget + space;
            }
            content = content.replaceAll(regex, traget);
        }
        return content;
    }

    /**
     * 处理Flag
     * @param content
     * @param flagVal
     * @return
     * @throws NumberFormatException
     */
    private String handleFlag(String content, Double flagVal) throws NumberFormatException {
        if(stop_flag != EnumStopFlag.NORMAL) {
            return content;
        }
        Matcher m2 = R_FLAG_M_PATTERN.matcher(content);
        if(m2.find()) {
            String findContent = m2.group(0);
            Matcher m2n = R_FLAG_M_PATTERN_2.matcher(findContent);
            if(m2n.find()) {
                String flag = m2n.group(0);
                double result = Double.valueOf(flag) + flagVal;
                if(result > RBFZ_MAX) {
                    stop_flag = EnumStopFlag.RB_RF_RZ;
                }
                if(result <= 0.0) {
                    stop_flag = EnumStopFlag.RB_RF_RZ_FS;
                }
                Double result2 = null;
                if(findContent.contains(RF_FLAG) || findContent.contains(RB_FLAG)) {
                    int random = random(rbfList.size());
                    double randomNum = rbfList.get(random) / 10000.0;
                    result2 = result + randomNum;
                }

                if(findContent.contains(RZ_FLAG)) {
                    int random = random(rzList.size());
                    double randomNum = rzList.get(random) / 10000.0;
                    result2 = result + randomNum;
                }

                String flagResult = formatDouble(result2,"##0.00000");

                content = content.replaceAll(R_FLAG_M_STR,findContent.replaceAll("\\d.\\d*",flagResult));
            }

        }
        return content;
    }

    /**
     * 对距离进行处理
     * @param content
     */
    private String handleDistance(String content,Double distance) {
        if(stop_flag != EnumStopFlag.NORMAL) {
            return content;
        }
        Matcher m3 = HD_DISTANCE_M_PATTERN.matcher(content);
        if(m3.find()) {
            String findContent = m3.group(0);
            Matcher m3n = HD_DISTANCE_M_PATTERN_2.matcher(findContent);
            if(m3n.find()) {
                String flag = m3n.group(0);
                double result = Double.valueOf(flag) + distance;
                String flagResult = formatDouble(result,"##0.000");

                final String space = addSpace(Math.abs(flag.length() - flagResult.length()));
                String regex = "\\d?\\d?\\d.\\d*";
                if(flag.length() > flagResult.length()) {
                    flagResult = space + flagResult;
                }

                if(flag.length() < flagResult.length()) {
                    regex = space + regex;
                }

                content = content.replaceAll(HD_DISTANCE_M_STR,findContent.replaceAll(regex,flagResult));
            }
        }

        return content;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        hourCB.setItems(FXCollections.observableArrayList(
                "01", "02", "03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","00"));

        minuteCB.setItems(FXCollections.observableArrayList(
                "01", "02", "03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23"
                ,"24","26","27","28","29","30","31","32","33","34","35","36","37","38","39","40","41"
                ,"42","43","44","45","46","47","48","49","50","51","52","53","54","55","56","57","58","59","00"));
        //初始化常数
        initConstNum();
    }

    private void initConstNum() {
        try {
            configModel = ConfigFileUtils.readConfigFile();

            String crypt = MD5Util.crypt(secretKey.getText() + NetworkUtil.allphyicalMACString());
            if(!StringUtils.equals(crypt,configModel.getLicence())) {
                Alert alert = new Alert(Alert.AlertType.ERROR,"许可错误！");
                alert.showAndWait();
                System.exit(0);
            }

            Range<Integer> rbfRange = Range.closed(configModel.getRbfStart(), configModel.getRbfEnd());
            rbfList = Lists.newArrayList();
            for(Integer i : ContiguousSet.create(rbfRange, DiscreteDomain.integers())) {
                rbfList.add(i);
            }

            Range<Integer> rzRange = Range.closed(configModel.getRzStart(), configModel.getRzEnd());
            rzList = Lists.newArrayList();
            for(Integer i : ContiguousSet.create(rzRange,DiscreteDomain.integers())) {
                rzList.add(i);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR,e.getMessage());
            alert.showAndWait();
            System.exit(0);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private String formatDouble(Double s,String format) {
        return new DecimalFormat(format).format(new BigDecimal(s).setScale(5,BigDecimal.ROUND_HALF_UP).doubleValue());
    }

    private String addSpace(int spaceNum) {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < spaceNum; i++) {
            result.append(" ");
        }
        return result.toString();
    }

    private String repairZero(String org) {
        if(org.length() == 1) {
            return "0" + org;
        }
        return org;
    }

    private Double sum(List<Double> doubleList) {
        return doubleList.stream().mapToDouble(Double::doubleValue).sum();
    }

    private Double average(List<Double> doubleList) {
        return sum(doubleList)/doubleList.size();
    }

    private Double calcZ(Double first,List<Double> rbList,List<Double> rfList) {
        return first + average(rbList) - average(rfList);
    }

    private Double lastItem(List<Double> items) {
        return items.get(items.size() - 1);
    }

    private int random(int flag) {
        return RANDOM.nextInt(flag);
    }
}
