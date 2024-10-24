// package com.example.instagram_spring_boot.util;

// import java.util.HashMap;
// import java.util.List;
// import java.util.Scanner;

// import io.lettuce.core.KeyValue;
// import io.lettuce.core.SetArgs;

// public class LettuceStringCmd {

//     Scanner scanner = new Scanner(System.in);
//     Util util = new Util();

//     public LettuceStringCmd() {

//         while (true) {
//             System.out.println("1:SET   2:GET   3:INCR   4:DECR   5:MSET  6:MGET\n");
//             System.out.println("선택(exit:99)> ");
//             String input = scanner.nextLine();
//             int i = util.checkInt(input);
//             switch (i) {
//                 case 1:
//                     set();
//                     break;
//                 case 2:
//                     get();
//                     break;
//                 case 3:
//                     incr();
//                     break;
//                 case 4:
//                     decr();
//                     break;
//                 case 5:
//                     mset();
//                     break;
//                 case 6:
//                     mget();
//                     break;
//                 case 99:
//                     break;
//                 default:
//                     System.out.println("잘못 선택했습니다. 다시 선택하세요. ");
//             }
//             if (i == 99) {
//                 break;
//             }
//         }
//     }

//     // SET key value [NX|XX] [EX 초] [PX 밀리초] [EXAT timestamp] [PXAT milliseconds-timestamp] [KEEPTTL] [GET]
//     // version 1.0.0 부터 사용할 수 있습니다.
//     // NX, XX, EX, PX 옵션은 버전 2.6.12에서 추가되었습니다.
//     // KEEPTTL 옵션은 버전 6.0에서 추가되었습니다.
//     // EXAT, PXAT, GET 옵션은 버전 6.2에서 추가되었습니다.
//     public void set() {
//         // NX: 키가 없을 경우에만 저장 -> insert only
//         // XX: 키가 이미 있을 경우에만 저장 -> update only
//         // EX: 지정한 seconds 이후에 키가 지워짐.
//         // PX: 지정한 milliseconds 이후에 키가 지워짐.
//         // KEEPTTL: TTL을 유지함.

//         System.out.println("SET key value [NX|XX] [EX 초] [PX 밀리초] [EXAT timestamp] [PXAT milliseconds-timestamp] [KEEPTTL] [GET]");
//         String input = scanner.nextLine();
//         String[] cmd = util.split(input);
//         if (cmd[0].equalsIgnoreCase("SET") == false) {
//             return;
//         }
//         SetArgs args = new SetArgs();
//         if (cmd.length > 3) {
//             if (cmd[3].equalsIgnoreCase("NX")) {
//                 args.nx();
//             } else if (cmd[3].equalsIgnoreCase("XX")) {
//                 args.xx();
//             } else if (cmd[3].equalsIgnoreCase("EX")) {
//                 if (cmd.length > 4) {
//                     long ex = util.checkLong(cmd[4]);
//                     args.ex(ex);
//                 }
//             } else if (cmd[3].equalsIgnoreCase("PX")) {
//                 if (cmd.length > 4) {
//                     long px = util.checkLong(cmd[4]);
//                     args.px(px);
//                 }
//             }
//         }
//         try {
//             String ret = Main.syncCmd.set(cmd[1], cmd[2], args);
//             System.out.println(ret);
//         } catch (Exception e) {
//             System.out.println(e.getMessage());
//             return;
//         }
//     }

//     public void set(String key, String value) {
//         try {
//             String ret = Main.syncCmd.set(key, value);
//             System.out.println(ret);
//         } catch (Exception e) {
//             System.out.println(e.getMessage());
//             return;
//         }
//     }

//     public void setArgs(String key, String value, long ex) {
//         SetArgs args = new SetArgs();
//         args.ex(ex);
//         try {
//             String ret = Main.syncCmd.set(key, value, args);
//             System.out.println(ret);
//         } catch (Exception e) {
//             System.out.println(e.getMessage());
//             return;
//         }
//     }

//     // GET key
//     // 이 명령은 version 1.0.0 부터 사용할 수 있다.
//     public void get() {
//         System.out.println("GET key");
//         String input = scanner.nextLine();
//         String[] cmd = util.split(input);
//         if (cmd[0].equalsIgnoreCase("GET") == false) {
//             return;
//         }
//         if (cmd.length != 2) {
//             return;
//         }

//         try {
//             String ret = Main.syncCmd.get(cmd[1]);
//             System.out.println("GET -> " + ret);
//         } catch (Exception e) {
//             System.out.println(e.getMessage());
//             return;
//         }
//     }

//     public void get(String key) {
//         try {
//             String ret = Main.syncCmd.get(key);
//             System.out.println(ret);
//         } catch (Exception e) {
//             System.out.println(e.getMessage());
//             return;
//         }
//     }

//     // INCR key
//     // 이 명령은 version 1.0.0 부터 사용할 수 있다.
//     public void incr() {
//         System.out.println("INCR key");
//         String input = scanner.nextLine();
//         String[] cmd = util.split(input);
//         if (cmd[0].equalsIgnoreCase("INCR") == false) {
//             return;
//         }
//         if (cmd.length != 2) {
//             return;
//         }

//         try {
//             Long ret = Main.syncCmd.incr(cmd[1]);
//             System.out.println("INCR -> " + ret);
//         } catch (Exception e) {
//             System.out.println(e.getMessage());
//             return;
//         }
//     }

//     // DECR key
//     // 이 명령은 version 1.0.0 부터 사용할 수 있다.
//     public void decr() {
//         System.out.println("DECR key");
//         String input = scanner.nextLine();
//         String[] cmd = util.split(input);
//         if (cmd[0].equalsIgnoreCase("DECR") == false) {
//             return;
//         }
//         if (cmd.length != 2) {
//             return;
//         }

//         try {
//             Long ret = Main.syncCmd.decr(cmd[1]);
//             System.out.println("DECR -> " + ret);
//         } catch (Exception e) {
//             System.out.println(e.getMessage());
//             return;
//         }
//     }

//     // MSET key1 value1 [key2 value2 ...]
//     // 이 명령은 version 1.0.1 부터 사용할 수 있다.
//     public void mset() {
//         System.out.println("MSET key1 value1 [key2 value2 ...]");
//         String input = scanner.nextLine();
//         String[] cmd = util.split(input);
//         if (cmd[0].equalsIgnoreCase("MSET") == false) {
//             return;
//         }
//         if (cmd.length < 3 || cmd.length % 2 == 0) {
//             return;
//         }
//         HashMap<String, String> msetMap = new HashMap<String, String>();
//         for (int i = 1; i < cmd.length; i++) {
//             msetMap.put(cmd[i], cmd[++i]);
//         }

//         try {
//             String ret = Main.syncCmd.mset(msetMap);
//             System.out.println("MSET -> " + ret);
//         } catch (Exception e) {
//             System.out.println(e.getMessage());
//             return;
//         }
//     }

//     // MGET key1 [key2 ...]
//     // 이 명령은 version 1.0.0 부터 사용할 수 있다.
//     public void mget() {
//         System.out.println("MGET key1 [key2 ...]");
//         String input = scanner.nextLine();
//         String[] cmd = util.split(input);
//         if (cmd[0].equalsIgnoreCase("MGET") == false) {
//             return;
//         }
//         if (cmd.length < 2) {
//             return;
//         }
//         String cmd2[] = util.split(input.substring(cmd[0].length() + 1));

//         try {
//             List<KeyValue<String, String>> ret = Main.syncCmd.mget(cmd2);
//             System.out.println("MGET -> " + ret.toString());
//         } catch (Exception e) {
//             System.out.println(e.getMessage());
//             return;
//         }
//     }
// }
