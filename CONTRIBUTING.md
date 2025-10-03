## How to Contribute

We welcome contributions to improve the Odin IntelliJ Plugin! Follow the steps below to get started:

1. **Fork this repository**  
   Create your own copy of this repository by clicking the "Fork" button in the top-right corner of this page.

2. **Generate the Parser Code**  
   Navigate to the `Odin.bnf` file, right-click on it, and select **"Generate Parser Code"** or use the shortcut `Ctrl+Shift+G`.

3. **Generate the Lexer Code**  
   Navigate to the `Odin.flex` file, right-click on it, and select **"Run JFlex Generator"** or use the shortcut `Ctrl+Shift+G`.
 
4. **Build**  
   Execute `./gradlew build`

5. **Create plugin**  
   Execute `$ ./gradlew plugin:publishPlugin`
   This will result in a zip file being created in `plugin/build/distributions`

That's it! You're now ready to start making contributions. Be sure to test your changes thoroughly before submitting a pull request. If you encounter
any issues or have questions, feel free to open an issue in this repository.