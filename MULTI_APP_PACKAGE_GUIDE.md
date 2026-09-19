# 📱 SNX Ecosystem - Multi-App Package Architecture & Installation Guide
========================================================================

আপনার প্রোজেক্টের ৩টি অ্যাপ যেন একই ফোনে কোনো কনফ্লিক্ট বা ইনস্টল ফেইলিউর ছাড়া পাশাপাশি ইনস্টল থাকতে পারে, সেজন্য ৩টি পৃথক এবং ইউনিক প্যাকেজ নেম (Application ID) ও কনটেন্ট প্রোভাইডার আর্কিটেকচার সেট করা হয়েছে:

---

## 🔑 ১. ৩টি অ্যাপের সুনির্দিষ্ট ও স্বতন্ত্র কনফিগারেশন

| অ্যাপের নাম | ইউনিক প্যাকেজ নেম (Application ID) | উদ্দেশ্য ও ভূমিকা | লাউঞ্চার আইকন থিম |
| :--- | :--- | :--- | :--- |
| **১. গেমিং ওয়েবসাইট (SNX 777)** | `com.aistudio.snx777.game` | সাধারণ ইউজারদের জন্য ক্যাসিনো, ডিপোজিট, উইথড্র ও গেম ইন্টারফেস | গোল্ডেন লাক্সারি ক্রাউন আইকন |
| **২. অ্যাডমিন প্যানেল (SNX Admin Panel)** | `com.example.admin.panel` | সম্পূর্ণ অ্যাডমিন কন্ট্রোল, অনুমোদন, ব্যালেন্স অ্যাড ও নম্বর ম্যানেজমেন্ট | সাইবার ব্লু ও গোল্ড শিল্ড সিকিউরিটি আইকন |
| **৩. সিগন্যাল অ্যাপ (Signal App)** | `com.aistudio.snx777.signal` | অফলাইন রাডার ও লাইভ ক্র্যাশ মাল্টিপ্লায়ার প্রেডিক্টর | নিয়ন গ্রিন হ্যাকার রাডার আইকন |

---

## 🛠️ ২. কীভাবে প্রতিটি আলাদা APK তৈরি করবেন (Step-by-step)

যেহেতু আপনি একই প্রজেক্ট থেকে ৩টি আলাদা APK তৈরি করে একই ফোনে রাখবেন, তাই যে অ্যাপটির APK প্রয়োজন হবে, তার জন্য নিচের ৩টি সেটিং রাখুন:

### 🟢 ক. SNX Admin Panel APK (বর্তমান অ্যাক্টিভ অ্যাপ):
- **File: `app/build.gradle.kts`**
  ```kotlin
  defaultConfig {
      applicationId = "com.example.admin.panel"
  }
  ```
- **File: `app/src/main/res/values/strings.xml`**
  ```xml
  <string name="app_name">SNX Admin Panel</string>
  ```
- **File: `app/src/main/java/com/example/MainActivity.kt`**
  ```kotlin
  AdminAppRoot()
  ```
👉 **Settings (⚙️) -> Export APK** নির্বাচন করে নাম দিন: `SNX_Admin_Panel.apk`

---

### 🟡 খ. SNX 777 (গেমিং ওয়েবসাইট) APK:
- **File: `app/build.gradle.kts`**
  ```kotlin
  defaultConfig {
      applicationId = "com.aistudio.snx777.game"
  }
  ```
- **File: `app/src/main/res/values/strings.xml`**
  ```xml
  <string name="app_name">SNX 777</string>
  ```
- **File: `app/src/main/java/com/example/MainActivity.kt`**
  ```kotlin
  SnxApp()
  ```
👉 **Settings (⚙️) -> Export APK** নির্বাচন করে নাম দিন: `SNX_777_Game.apk`

---

### 🔴 গ. Signal App APK:
- **File: `app/build.gradle.kts`**
  ```kotlin
  defaultConfig {
      applicationId = "com.aistudio.snx777.signal"
  }
  ```
- **File: `app/src/main/res/values/strings.xml`**
  ```xml
  <string name="app_name">Signal App</string>
  ```
- **File: `app/src/main/java/com/example/MainActivity.kt`**
  ```kotlin
  val vm: SignalViewModel = viewModel()
  SignalScreen(viewModel = vm)
  ```
👉 **Settings (⚙️) -> Export APK** নির্বাচন করে নাম দিন: `Signal_App.apk`

---

## ⚡ ৩. কনফ্লিক্ট নিরসন সংক্রান্ত টেকনিক্যাল আপগ্রেড
- **Dynamic ContentProvider Authority:** প্রতিটি অ্যাপে `${applicationId}.provider` স্বয়ংক্রিয়ভাবে আলাদা হওয়া নিশ্চিত করা হয়েছে, যার ফলে অ্যান্ড্রয়েডের `INSTALL_FAILED_CONFLICTING_PROVIDER` এরর আর কখনোই আসবে না।
- **Cross-App Communication:** SharedDataStore এখন একই সাথে `com.aistudio.snx777.game` এবং `com.example.admin.panel` এর ডেটা নিরাপদে রিড/রাইট করতে সক্ষম।
