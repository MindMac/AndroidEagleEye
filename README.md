Android EagleEye
========

An [Xposed](http://repo.xposed.info/) based module which is capable of hooking both Android system APIs and applications' methods. Related information of the hooked APIs or methods will be logged as the output.

**Using Android EagleEye is entirely at your own risk**

<img src="screenshots/overview.png" width="400" height="256"/>

Features
--------
* Able to hook both Android system APIs and applications' methods
* Hook APIs and methods on demand
* Hook methods dynamically loaded through DexClassLoader
* Adopt methods against anti-emulator

Contents
--------
* EagleEye: Source code of the Android EagleEye
* EagleEyeTest: A test application for EagleEye
* testcase_configs: Configures of customized Android system APIs and application's methods need to be hooked(for EagleEyeTest)

Usage
--------

**Ensure you have root access and have installed [XposedInstaller](http://repo.xposed.info/module/de.robv.android.xposed.installer)**

Follow the steps below:

1. Install *EagleEye.apk* and enable this module in XposedInstaller
2. Start the EagleEye application to initialize environments. If initialization fails, you can follow the instructions below to manually complete the environment initialization
	* $ *adb shell su -c mount -o rw,remount /system*
	* $ *adb push EagleEye/assets/libfd2path.so /system/lib/*
	* $ *adb shell su -c chmod 777 /system/lib/libfd2path.so*
3. Reboot the Android device
4. Set the property *rw.eagleeye.targetuids* to the *uids* of the applications you want to hook,for example:
	* *adb shell su -c setprop rw.eagleeye.targetuids "10076|10078"* (uids are splitted by "|")
5. **Restart** the applications you want to hook (EagleEye will read the *rw.eagleeye.targetuids* property whenever the application loaded, so if you want to hook other applications, you only need set this property to the appropriate value and restart the applications)
6. Use *adb logcat -s EagleEye:I* to see the log information

**EagleEye has predefined some Android system APIs to hook by default(refer to the classes under *com.mindmac.eagleeye.hookclass* package), if you want to hook other system APIs or applications' methods, you can configure as following:**

1. The Android system APIs can be customized by the **[system_apis.config](#"Configure File Format")** (Refer to *Configure File Format* section for detail). Please push this configure file to the Android device's directory **/data/local/tmp/**. The default number of APIs you can customize is limited to 500, and you can set the property **rw.eagleeye.system_api_num** to the value as you want.
2. The application's methods can be customized by the **[app_apis.config](#"Configure File Format")**  (Refer to *Configure File Format* section for detail). Please push this configure file to the Android device's directory **/data/data/\<package name of the application you want to hook\>/**. The default number of methods you can customize is limited to 500, and you can set the property **rw.eagleeye.app_api_num** to the value as you want.
3. **Restart** the application you want to hook (EagleEye will reload the configure files when the application loaded).


Anti Anti-Emulator
--------
Some Android applications will check the runtime environment to see if they are running on the Android emulator. EagleEye adopts some methods based on *runtime hook* against such anti-emulator technique. Take the [anti-emulator](https://github.com/strazzere/anti-emulator) project authored by Tim Strazzere for example, the output is shown as below after deploying EagleEye:

V/AntiEmulator( 2226): Checking for Taint tracking...  
V/AntiEmulator( 2226): hasAppAnalysisPackage : false  
V/AntiEmulator( 2226): hasTaintClass : false  
V/AntiEmulator( 2226): hasTaintMemberVariables : false  
V/AntiEmulator( 2226): Taint tracking was not detected.  
V/AntiEmulator( 2226): Checking for Monkey user...  
V/AntiEmulator( 2226): isUserAMonkey : false  
V/AntiEmulator( 2226): Monkey user was not detected.  
V/AntiEmulator( 2226): Checking for debuggers...  
V/AntiEmulator( 2226): No debugger was detected.  
V/AntiEmulator( 2226): Checking for QEmu env...  
V/AntiEmulator( 2226): hasKnownDeviceId : false  
V/AntiEmulator( 2226): hasKnownPhoneNumber : false  
V/AntiEmulator( 2226): isOperatorNameAndroid : false  
V/AntiEmulator( 2226): hasKnownImsi : false  
V/AntiEmulator( 2226): hasEmulatorBuild:false  
V/AntiEmulator( 2226): hasPipes : false  
*V/AntiEmulator( 2226): hasQEmuDriver : true*  
V/AntiEmulator( 2226): hasQEmuFiles : false   
*V/AntiEmulator( 2226): hasEmulatorAdb :true*   
*V/AntiEmulator( 2226): hitsQemuBreakpoint : true*  
V/AntiEmulator( 2226): QEmu environment detected.

There are still several anti-emulator methods which EagleEye does not take into consideration currently.


Configure File Format
--------
The **system_apis.config** and **app_apis.config** are both in the same format.
Described as following:

1. APIs or methods you want to hook are separated by lines.
2. The number of APIs or methods EagleEye will read is constrained by the property **rw.eagleeye.system_api_num** or **rw.eagleeye.app_api_num**. The default value is set to 500.
2. Each API or method is in the *smali* format. For example, if you want to hook [Intent.putExtra()](http://developer.android.com/reference/android/content/Intent.html#putExtra(java.lang.String, java.lang.String)), the content line will look like this:
	* Landroid/content/Intent;->putExtra(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;
3. The parameters and return type can be left blank to hook all the APIs or methods with the same method name. For example, you can configure the content line as below to hook all the *putExtra* methods under [Intent](http://developer.android.com/reference/android/content/Intent.html) class:
	* Landroid/content/Intent;->putExtra
	
Refer to the configure files under the [testcase_configs](testcase_configs) for more detail.

Log Format
--------
EagleEye will log the parameters' value of the hooked APIs or methods in JSON format. The hooked application's *uid*, *hook type(system api* or *app method)* and *if the api customized* will be logged. For predefined Android system APIs, parameters' names will also recorded, while parameters' types, return type and value will be recorded for customized APIs or methods.

A typical log is shown as below:

*{"Uid":"10047", "HookType":"system_api", "Customized":"false", "InvokeApi":{"libcore.io.IoBridge->open":{"path":"/data/data/com.mindmac.eagleeyetest/app_apis.config", "flags":"0"}}}*

*{"Uid":"10047", "HookType":"system_api", "Customized":"true", "InvokeApi":{"Landroid/app/ContextImpl;->getPackageName":{"parameters":{}, "return":{"java.lang.String":"com.mindmac.eagleeyetest"}}}}*

*{"Uid":"10047", "HookType":"app_api", "Customized":"true", "InvokeApi":{"Lcom/mindmac/eagleeyetest/MainActivity;->add":{"parameters":{"int":"5", "int":"7"}, "return":{"int":"12"}}}}*

Since Android's Logcat will truncate the log if it exceeds 1024 bytes, EagleEye will devide the content of file read and write operation into multiple parts.You can reconstruct the content based on the *id* value. The target file path and file content are both in hex bytes. Records of file operations are shown as below:

*{"Uid":"10047", "HookType":"system_api", "FdAccess": {"path": "2f6d6e742f7364636172642f44796e616d69632e61706b", "id": "1094768956" } }*

*{"Uid":"10047", "HookType":"system_api", "Customized":"false","FileRW":{ "operation": "write", "data": "504b0304140008080800f4b0d6463100c38daf020000fc06000013000400416e64726f69644d616e69666573742e786d6cfeca00008d543d6f1341149cbdb3938bf381f369c736120515c2074401457449400a52a0004145636207ac248e655f42a021252525053f00217e00250505bf809a9a3a652498f76e8f5b5f1cc19dc6bb3b3b3befeddb3dfb08703a0218d4f0dd072e227dde39fd0ab14c3c240eeddc07e213f185f846fc2042032c11778823e233f195f8459c10a744c5a38ed8209e12af89f7c447e22771428c334a0b3df4d1c63e3a58e76f93ccd9990768604f6726d9b6c93ca272074f0654401111953d3c271f9da319a76297ef3e5e628dfd2d6a0ed0e54c8e9a2dabca53d1c033aeddd5518417ecc739e4a848f21965afc3383dae6bb305ae5019f1ede236aef1edd3335edb603fcce8438db8475d832b76d8f6a8edeb78d0171aad6b336ee81e813219c934c236953d3aad71376d72cdccced3eafeff9ab4ee81e6df21bfadf945e4666dee219923558a6f8be3265ed91a4945011f37c85e67cfc312fb728e37950939aa13b7c82febbba2d10e1843e2d40939c5f8dcba1a413c25fbe44403ad898c0f8988b1c53fc47df272575687cc0a1f3182fcd6b927d97da47b0746fefac5fe953367e6ae0e07d4127515f75839c92bceb3c5b3923a4be44bfff0caae08b149bfc7f45bc706eeeab7796c025c655bf58ca91113449738263c1863087eedc697d61479de5277e0379f31b60519937febf0f25c607f8e6fdedeb6826d652ee0c7652c27edb4ee0ffeb49d9fd0af2fe68a969b643b1373c579cb4de9bd8975b3368f4dbdd9691e73360fcfc9c373f298b29cef70e2efdbbd8e65bc2446a43728e5e76d8c9c136334f563e146de14f42ea41cff0794cb0f709e72b9949b9136d957d189b930645fc3fcd947c9d6a864f3bf6ceb967895ad57f2886645b954b36835c68917d8ff79f1ae3835cbae4bfc161cbe7a8e5fcdfad51cbfecba842f65f690f0e54c9d127e317366095fcddc5b93e1937bfe07504b07083100c38daf020000fc060000504b03040a0000080000568a", "id": "1094768956"}}*

*{"Uid":"10047", "HookType":"system_api", "Customized":"false","FileRW":{ "operation": "write", "data": "d54662df48f24c1700004c170000240002007265732f6472617761626c652d686470692d76342f69635f6c61756e636865722e706e67000089504e470d0a1a0a0000000d494844520000004800000048080600000055edb347000017134944415478daed5b69905dc575fe4ef75dde326f66342369b401328b8d05212c31066313f00201dbb81c22657116d9a932492a4e2524ae2cae0a0f278e718c8dcb29db31210976e2251a6f0a0107a41805634258a25844022421242446d268f6b7dd7bbbfb9cfce8fb16c9d81ac733b64971abeebc59eebc77fbeb73beef3ba7fb022f1d2f1d2f1d2f1dff4f0f916ab027fdfd759bf75d3722025ad8f77e77f87476c34f6e3df4f6e1c51c835a3c7040ffb2ebf155bbf7effbebac9ebdefe66d6be3938124029a0f902255b5d3b80b0e8d4f7de1c96f8fff6a55aa6aa12760d1010280c7b78fcfec7ef2c8ae2cb5bf7266e5f48db73f7e51d01da4507bb0229bb4882860bd1ac57a2522caffde0f5c443ac089807635c6971f9bacddb2f3bfc7069efeafdab7aba80a116431c6102c1638441091ffac7df0ce4b3f562fa76fa82c897f2f6c15ef13a9eeef62f47b4583152f076a17103ebd92f1fafeb721a40c7fd710ac19cfc04f1cc32d3b4f07e6006093acd7bb005dcb66371e3e347dd1d8fee6a732173ebc38b1938f63b179a85aadaaf8ac7baf1f5e19de1ec56aeb9983a7fefe25eb2e3dcf22bd96e1de2230ab1d2c010286411313c8d002a001c4000a53cee9fb8ce37b9ab6fc8da4b9efac23e3935fddf1c8f88e47b7a46f7fd5cbaf9fad56abfca2050802baedce2b069a61fd96534eeffbf9cbcfb978d7507fe5150a6a10d0ca2121861382c0c2a1854934310d0786858513013b2d9c857576e5c7a6a666fb9f7874ec94677737de3962aebf7731c159d414ebe253a5f18d76d836c2208bf695fa43ba14509221510a408c214458420a1118161a87902003c3003010b6b0d6927169c59a992bc302c9d295f1b70787fa67c7be59850868b1f807791c2fa6ccab43983dbd1f239fa0a8f9b6580f840a054a3043050ca00fa722c410081104", "id": "1094768956"}}*












