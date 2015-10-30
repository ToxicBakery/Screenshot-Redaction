# Screenshot Redaction
Avoid leaking sensitive information when sharing screenshots. Security is hard, let your device try to help you. Using Tesseract and automatic processing of screenshots, simply review, edit, and share redacted versions of your screenshots.

## Background
In several Slack groups (chat rooms) I have witnessed people mistakenly share sensitive information like email addresses, home addresses, and phone numbers. As sharing information becomes easier it is becoming increasingly difficult to self monitor. Screenshot Redaction aims to keep users cognizant of what they are potentially sharing automatically and with limited friction.

## Building
This project is built with Gradle using the Gradle Wrapper script.

*Linux*  
`./gradlew assembleDebug`

*Windows*  
`gradlew.bat assembleDebug`

## How Redaction Works
The redaction process is currently mostly static and fairly simple. In the future the process will be more flexible allowing submission of photos for processing or even regions of photos. The process initially uses Tesseract OCR to find words inside the image. Once this process is finished, users are notified of completion. If a user chooses to view the redactions, the currently enabled word dictionaries are applied to the results. Dictionaries can choose to white list or black list with their own internal rules. The end result is a screenshot with zero or more words wrapped in boxes and blacked out.

### Trained Languages
Currently, English is the only trained language that is implemented in the application. The application settings holds a placeholder for future languages though no backend functionality exists yet for downloading and using alternate language. To support this feature, dictionaries will first need to be expanded for other languages and the languages training data will likely need to be packaged with the dictionaries to prevent bloating the APK further. If you are interested in trying new languages or updating the English langauge, you can follow the [Tesseract guides for training](https://github.com/tesseract-ocr/tesseract/wiki/TrainingTesseract) or use the list of [prebuilt training data](https://github.com/tesseract-ocr/tessdata).

### Dictionaries
The dictionaries are plain text flat files with one word on each line. Dictionaries should not have any blank lines and should be saved to the `app/resources` directory using a name beginning with `wordlist_` and underscores instead of spaces.

When the project is built, the dictionary will be converted to a MapDB store and save to the project raw resources. Changes to dictionaries are also monitored and will result in the dictionary map being rebuilt.

Dictionaries by themselve have no function and require a concrete implementation. Once implemented the dictionary currently needs to be added to the dictionary provider implementation. This is a termporary limitation that will use simple rules and a mapping dictionary in the future.

### Licenses
Licenses are stored in the raw resources and require manual addition to the `licensing_list` JSON Object.
