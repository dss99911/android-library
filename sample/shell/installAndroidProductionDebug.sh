./gradlew -Penvironment=production :sample-android:installFreeProdDebug && \
adb shell am start  -n "kim.jeonghyeon.sample.compose/com.example.sampleandroid.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER