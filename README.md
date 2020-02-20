# AB-SoundLibrary  

1. Define Delegator.  

2. implements the Callback Interface.  

3. initialize delegator with parameters (Application context “this”, language ("ar" , "en"),callback ”this”).  

4. invoke method startListen().  

5. onDone method after the result returns.  

6. in case of new data add it to data2.json file at assets.    
	{  
  "codes": [  
    {  
      "id": "عروض",  
      "code": "0001"  
    },  
    {  
      "id": "transfer",  
      "code": "0005"  
    }  
    	   ]  
	}  
  
  how to import :

1. Goto File -> New -> Import Module.  
2. Source Directory -> Browse the project path.
3. Specify the Module Name – it is used for internal project reference.
4. Let Android Studio build the project.
5. Open build.gradle (Module:app) file.
6. Add the following line with your module name in the dependencies block:  
	implementation project(path: ':soundlibrary')


7. add  
	repositories {  
  	    maven { url "https://jitpack.io" }  
	}


8. add to android   
    compileOptions {  
        sourceCompatibility = 1.8  
        targetCompatibility = 1.8  
     }  
     
9. check that the project minSdkVersion is 16

10. Sync project
