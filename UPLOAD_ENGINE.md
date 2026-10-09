# آپلود موتور کامل TITAN (الزامی برای APK فول)

فایل `TITAN_PRO_V75_PWA.py` حدود ۱٫۴ مگابایت است و باید **یک‌بار** در گیت‌هاب آپلود شود.

## روش ساده (وب)

1. برو به: https://github.com/ehsanrahmanii-code/TITAN-Pro-Android
2. پوشه `app/src/main/python/` را باز کن
3. **Add file → Upload files**
4. فایل `TITAN_PRO_V75_PWA.py` را بکش و رها کن
5. Commit changes

## بعد از آپلود

Actions → **Build APK** خودکار اجرا می‌شود.
Artifact حدود **۳۰ تا ۷۰ مگابایت** خواهد بود (numpy + pandas + flask + موتور).

## ساختار

```
app/src/main/python/
  titan_bootstrap.py      ← راه‌انداز (از قبل هست)
  TITAN_PRO_V75_PWA.py    ← همین فایل خودت (باید آپلود کنی)
```
