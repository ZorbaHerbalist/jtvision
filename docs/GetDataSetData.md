# GetData and SetData

This document describes how data is transferred between views and the application,
based on the Turbo Vision library and a proposed implementation in JTVision.

## Turbo Vision (contrib/bp)

- `TView`
  - `GetData` and `SetData` are virtual and empty by default.
  - `DataSize` returns `0` when a derived type exposes no data.
- `TGroup`
  - Overrides `GetData`/`SetData` and iterates over all child views.
  - The buffer size equals the sum of `DataSize` for all children.
- Example derived types:
  - `TInputLine` writes and reads text.
  - `TCluster` transfers an integer representing the selection.
- Dialogs (`TProgram.ExecuteDialog`) pass a data record to the view
  before execution (`SetData`) and read it back after closing (`GetData`).

## Suggested implementation in JTVision

1. **Interface in `TView`**
   - `int dataSize()` returns the size of view data; default `0`.
   - `void getData(ByteBuffer dst)` and `void setData(ByteBuffer src)` are no-ops.
   - A `ByteBuffer` (e.g., `DataPacket`) enables sequential copying of data.
2. **`TGroup`**
   - `dataSize()` sums the sizes of all child views.
   - `getData`/`setData` call the corresponding methods on each child,
     advancing the buffer position.
3. **Derived types**
   - Views that store state override all three methods.
   - Examples: `TInputLine`, `TCluster`, `TListBox`, `TColorDialog`.
4. **Integration with dialogs**
   - Before `execView(dialog)` the application calls `dialog.setData(buf)`.
   - After the dialog closes `dialog.getData(buf)` updates the record.
5. **Advantages**
   - Compatibility with Turbo Vision makes existing examples easier to port.
   - A uniform buffer simplifies data transfer between forms.
   - Validation or future extensions can be added later.
