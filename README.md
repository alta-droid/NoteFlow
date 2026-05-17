# NoteFlow 📝

تطبيق ملاحظات أندرويد احترافي مبني بـ Kotlin + Jetpack Compose

---

## المميزات

- 🗂️ **شبكة ملاحظات** بتصميم Staggered Grid جميل
- 🎨 **10 ألوان** يمكن اختيارها لكل ملاحظة
- ⏰ **تذكيرات** بإشعارات محلية في الوقت المحدد
- 🔍 **بحث** في العنوان والمحتوى
- 📌 **تثبيت** الملاحظات المهمة
- 🎬 **انيميشن** سلس عند التنقل بين الشاشات
- 💾 **تخزين محلي** بقاعدة بيانات Room
- 🌙 **Dark Theme** احترافي

---

## كيفية بناء التطبيق (APK)

### الطريقة 1: Android Studio (الأسهل)

1. حمّل وثبّت [Android Studio](https://developer.android.com/studio)
2. افتح Android Studio واختر **Open** ثم اختر مجلد `NoteFlow/`
3. انتظر حتى ينتهي Gradle من المزامنة (دقيقتان تقريباً)
4. من القائمة: **Build → Build Bundle(s)/APK(s) → Build APK(s)**
5. ستجد الـ APK في:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```
6. انقله لهاتفك وثبّته!

> ⚠️ تأكد من تفعيل "تثبيت من مصادر غير معروفة" في إعدادات الهاتف

### الطريقة 2: سطر الأوامر (إذا كان Android SDK مثبتاً)

```bash
cd NoteFlow
./gradlew assembleDebug
```

---

## هيكل المشروع

```
app/src/main/java/com/noteflow/app/
├── data/          # Room Database (Note, NoteDao, NoteDatabase)
├── repository/    # NoteRepository
├── viewmodel/     # NoteViewModel
├── notification/  # NotificationHelper, ReminderReceiver, ReminderScheduler
├── ui/
│   ├── theme/     # Color, Type, Theme
│   ├── screens/   # HomeScreen, CreateEditScreen, NoteDetailScreen
│   └── components/# NoteCard
├── MainActivity.kt
└── NoteFlowApp.kt
```

---

## التقنيات المستخدمة

| التقنية | الاستخدام |
|---|---|
| **Kotlin** | لغة البرمجة الرئيسية |
| **Jetpack Compose** | واجهة المستخدم |
| **Material Design 3** | نظام التصميم |
| **Room Database** | التخزين المحلي |
| **AlarmManager** | جدولة التذكيرات |
| **MVVM** | بنية المشروع |
| **Coroutines + Flow** | البرمجة غير المتزامنة |

---

## متطلبات النظام

- **minSdk**: Android 8.0 (API 26)
- **targetSdk**: Android 14 (API 34)
