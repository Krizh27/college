# Personal Expense Tracker — JavaFX Setup

This project was converted to use JavaFX. The UI entry is `Ui.MainView`.

Quick steps for Windows (manual JavaFX SDK):

1. Install a JDK (Java 17+ recommended).
2. Download the JavaFX SDK from Gluon / OpenJFX and extract it. Example target path: `C:\javafx-sdk-21`.

Compile and run from the command prompt (from the project root):

Windows (cmd):

```bat
set PATH_TO_FX=C:\javafx-sdk-21\lib
javac --module-path %PATH_TO_FX% --add-modules javafx.controls,javafx.fxml -d out src\**\*.java
java --module-path %PATH_TO_FX% --add-modules javafx.controls,javafx.fxml -cp out Main
```

Notes:
- If `javac` with the wildcard doesn't work in `cmd`, compile using your IDE (IntelliJ/VS Code) or a build tool.
- In IDE run configurations (IntelliJ/VS Code), add VM options:

```
--module-path "C:\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml
```

Maven/Gradle:
- Prefer adding OpenJFX dependencies via Maven or Gradle if you use a build tool; this README omits full snippets but can be added on request.

Files changed:
- `src/Main.java`
- `src/Ui/MainView.java` (UI)
- `src/Service/ExpenseService.java` (exposed `getExpenses()`)

See the UI entry at [src/Ui/MainView.java](src/Ui/MainView.java).
