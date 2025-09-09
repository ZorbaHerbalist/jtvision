program FileListViewerDemo;

{$M 16384,0,655360}

uses
  App, Objects, Drivers, Views, Menus, Dialogs, StdDlg, MsgBox, Dos;

const
  cmShowFileList = 1001;
  cmRefresh      = 1002;

type
  { Simple string collection to back the TListViewer }
  PStrCollection = ^TStrCollection;
  TStrCollection = object(TStringCollection)
  end;

  { TFileListViewer - a TListViewer that shows file metadata lines }
  PFileListViewer = ^TFileListViewer;
  TFileListViewer = object(TListViewer)
    Items: PStrCollection;
    constructor Init(var Bounds: TRect; ANumCols: Integer; AHScroll, AVScroll: PScrollBar; AItems: PStrCollection);
    destructor Done; virtual;
    function GetText(Item: Integer; MaxLen: Integer): String; virtual;
    procedure SetItems(AItems: PStrCollection);
  end;

  { Dialog containing: path input, label, list viewer with scrollbars, buttons }
  PFileListDialog = ^TFileListDialog;
  TFileListDialog = object(TDialog)
    PathInput : PInputLine;
    Viewer    : PFileListViewer;
    HScroll   : PScrollBar;
    VScroll   : PScrollBar;
    PathBuf   : String[128];
    constructor Init(const ATitle: String; const InitialPath: String);
    destructor Done; virtual;
    procedure HandleEvent(var Event: TEvent); virtual;
    procedure LoadDir(const DirPath: String);
  end;

  { Main application }
  TMyApp = object(TApplication)
    constructor Init;
    procedure InitMenuBar; virtual;
    procedure HandleEvent(var Event: TEvent); virtual;
    procedure ShowFileListDialog;
  end;

{ --------------------------- Utilities --------------------------- }

function PadRight(const S: String; W: Byte): String;
{ Pads S on the right with spaces to width W }
var
  R: String;
begin
  R := S;
  while Length(R) < W do R := R + ' ';
  PadRight := Copy(R, 1, W);
end;

function Two(n: Word): String;
{ Returns a 2-digit string, zero-padded (e.g., 7 -> '07') }
var
  s: String;
begin
  Str(n:0, s);
  if n < 10 then s := '0' + s;
  Two := s;
end;

function Four(n: Word): String;
{ Returns a 4-digit string, zero-padded (e.g., 89 -> '0089') }
var
  s: String;
begin
  Str(n:0, s);
  while Length(s) < 4 do s := '0' + s;
  Four := s;
end;

function FormatFileLine(const SR: SearchRec): String;
{ Formats one line: NAME.EXT  SIZE  YYYY-MM-DD HH:MM }
var
  dt    : DateTime;
  sizeS : String;
  name  : String;
  dateS : String;
begin
  UnpackTime(SR.Time, dt);
  Str(SR.Size:0, sizeS);
  name  := SR.Name;
  dateS := Four(dt.Year) + '-' + Two(dt.Month) + '-' + Two(dt.Day) +
           ' ' + Two(dt.Hour) + ':' + Two(dt.Min);

  FormatFileLine :=
    PadRight(name, 20) + '  ' +
    PadRight(sizeS, 10) + '  ' +
    PadRight(dateS, 17);
end;

function BuildFileList(const DirPath: String): PStrCollection;
{ Scans DirPath for files (non-directories) and returns a string collection of formatted lines }
var
  col : PStrCollection;
  sr  : SearchRec;
  mask: String;
  attr: Word;
begin
  New(col, Init(10, 10));

  mask := FExpand(DirPath);
  if mask = '' then mask := '.';
  if mask[Length(mask)] in ['\', '/'] then
    mask := mask + '*.*'
  else
    mask := mask + '\*.*';

  attr := AnyFile;

  FindFirst(mask, attr, sr);          { procedure, not a function }
  while DosError = 0 do               { keep iterating while no error }
  begin
    { Skip directories; accept regular/hidden/system/readonly/archive files }
    if (sr.Attr and Directory) = 0 then
      col^.Insert(NewStr(FormatFileLine(sr)));
    FindNext(sr);                     { also a procedure; updates DosError }
  end;

  BuildFileList := col;
end;

{ ------------------------ TFileListViewer ------------------------ }

constructor TFileListViewer.Init(var Bounds: TRect; ANumCols: Integer; AHScroll, AVScroll: PScrollBar; AItems: PStrCollection);
begin
  { Correct TV signature: (Bounds, ANumCols, HScroll, VScroll) }
  inherited Init(Bounds, ANumCols, AHScroll, AVScroll);
  Items := nil;
  SetItems(AItems);
end;

destructor TFileListViewer.Done;
begin
  if Items <> nil then
  begin
    Items^.FreeAll;
    Dispose(Items, Done);
  end;
  inherited Done;
end;

function TFileListViewer.GetText(Item: Integer; MaxLen: Integer): String;
{ Returns the display text for the given item index }
begin
  if (Items <> nil) and (Item >= 0) and (Item < Items^.Count) then
    GetText := Copy(PString(Items^.At(Item))^, 1, MaxLen)
  else
    GetText := '';
end;

procedure TFileListViewer.SetItems(AItems: PStrCollection);
{ Replaces the current Items collection and updates the range }
begin
  if Items <> nil then
  begin
    Items^.FreeAll;
    Dispose(Items, Done);
  end;
  Items := AItems;
  if Items <> nil then
    SetRange(Items^.Count)
  else
    SetRange(0);
  FocusItem(0);
  DrawView;
end;

{ ------------------------ TFileListDialog ------------------------ }

constructor TFileListDialog.Init(const ATitle: String; const InitialPath: String);
var
  r: TRect;
begin
  r.Assign(10, 3, 70, 20);
  inherited Init(r, ATitle);
  Options := Options or ofCentered;

  { Path input line – create this first }
  r.Assign(10, 2, 56, 3);
  PathInput := New(PInputLine, Init(r, 128));
  Insert(PathInput);

  { Wire Data to a persistent buffer and set initial value }
  PathBuf := InitialPath;
  PathInput^.SetData(PathBuf);

  { Path label – link directly via constructor (third arg) }
  r.Assign(2, 2, 10, 3);
  Insert(New(PLabel, Init(r, '~F~older:', PView(PathInput))));

  { Horizontal scroll bar }
  r.Assign(2, 15, 56, 16);
  HScroll := New(PScrollBar, Init(r));
  Insert(HScroll);

  { Vertical scroll bar }
  r.Assign(56, 4, 57, 15);
  VScroll := New(PScrollBar, Init(r));
  Insert(VScroll);

  { File list viewer (note: VScroll first, then HScroll) }
  r.Assign(2, 4, 56, 15);
  Viewer := New(PFileListViewer, Init(r, 1, HScroll, VScroll, nil));
  Insert(Viewer);

  { Buttons: OK and Refresh }
  r.Assign(58, 14, 68, 16);
  Insert(New(PButton, Init(r, 'OK', cmOK, bfDefault)));

  r.Assign(58, 11, 68, 13);
  Insert(New(PButton, Init(r, 'Refresh', cmRefresh, bfNormal)));

  { Put initial path and load }
  PathInput^.Data^ := InitialPath;
  LoadDir(InitialPath);
end;

destructor TFileListDialog.Done;
begin
  inherited Done;
end;

procedure TFileListDialog.LoadDir(const DirPath: String);
{ Builds the file list for DirPath and updates the viewer }
var
  col: PStrCollection;
begin
  col := BuildFileList(DirPath);
  Viewer^.SetItems(col);
end;

procedure TFileListDialog.HandleEvent(var Event: TEvent);
{ Reacts to Refresh and Enter key in the path field }
var
  dir: String[255];
begin
  inherited HandleEvent(Event);
  if Event.What = evCommand then
  begin
    case Event.Command of
      cmRefresh:
        begin
          PathInput^.GetData(dir);
          LoadDir(dir);
          ClearEvent(Event);
        end;
    end;
  end
  else if (Event.What = evKeyDown)
     and (Event.KeyCode = kbEnter)
     and (Current = PView(PathInput)) then
begin
  PathInput^.GetData(dir);
  LoadDir(dir);
  ClearEvent(Event);
end;
end;

{ --------------------------- TMyApp --------------------------- }

constructor TMyApp.Init;
begin
  inherited Init;
end;

procedure TMyApp.InitMenuBar;
var
  r: TRect;
begin
  GetExtent(r);
  r.B.Y := r.A.Y + 1;
  MenuBar := New(PMenuBar, Init(r,
    NewMenu(
      NewSubMenu('~F~ile', hcNoContext, NewMenu(
        NewItem('~S~how File List...', '', kbNoKey, cmShowFileList, hcNoContext,
        NewItem('E~x~it', 'Alt-X', kbAltX, cmQuit, hcNoContext, nil)))
      , nil)
    )
  ));
end;

procedure TMyApp.ShowFileListDialog;
var
  Dlg: PFileListDialog;
  InitialPath: String;
begin
  InitialPath := FExpand('.');
  Dlg := New(PFileListDialog, Init('File List (TListViewer)', InitialPath));
  if ValidView(Dlg) <> nil then
  begin
    ExecuteDialog(Dlg, nil);
    Dispose(Dlg, Done);
  end;
end;

procedure TMyApp.HandleEvent(var Event: TEvent);
begin
  inherited HandleEvent(Event);
  if Event.What = evCommand then
  begin
    case Event.Command of
      cmShowFileList:
        begin
          ShowFileListDialog;
          ClearEvent(Event);
        end;
    end;
  end;
end;

{ --------------------------- Main --------------------------- }

var
  MyApp: TMyApp;

begin
  MyApp.Init;
  MyApp.Run;
  MyApp.Done;
end.
