# Palette Guide

## Automatic index assignment

Palette roles derive their palette indices automatically from the order of the
enum constants. The `PaletteRole` interface provides the `defaultIndex()`
implementation which returns `ordinal() + 1` for enums, so new palette
definitions no longer have to hardcode numerical indices. Existing enums that
require legacy numbering can still override `index()` (or `defaultIndex()`)
explicitly.

Palette data is now sourced exclusively from JSON files stored in
`src/main/resources/palettes`. Register a palette by calling
`PaletteDescriptor.register("paletteName", MyPaletteRole.class)` and ensure the
corresponding `paletteName.json` file contains an entry for every enum constant.

## Handling missing entries

By default, missing or invalid palette indexes are reported as warnings and the
renderer falls back to the emergency attribute (`0xCF`). This preserves
compatibility with existing palette data even when an entry was omitted or maps
to the zero color.

If you want these situations to be treated as programming errors, enable
**strict mode**. In strict mode the first missing palette entry causes an
`IllegalStateException` and rendering stops so the faulty palette can be fixed.

### Enabling strict mode

Strict mode can be configured in two different ways:

* **System property** – start the application with
  `-Djtvision.palette.strict=true` to switch to the exception-throwing
  behaviour globally.
* **API** – set the policy explicitly during application initialisation:

  ```java
  PaletteFactory.setMissingEntryPolicy(PaletteFactory.MissingEntryPolicy.THROW);
  ```

Both the system property and the explicit API call apply process-wide and cover
all views that rely on palette mapping.

When compatibility mode (the default) is active, the warning message includes
both the missing index and the full view chain that attempted to resolve it so
that the origin of the issue can be located without stopping the application.
