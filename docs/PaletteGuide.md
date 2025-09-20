# Palette Guide

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
