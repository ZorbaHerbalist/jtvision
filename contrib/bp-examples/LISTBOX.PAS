program ListBoxDemo;

{$M 16384,0,655360}

uses
  App, Objects, Drivers, Views, Menus, Dialogs, StdDlg, MsgBox;

const
  cmShowListBox = 1001;
  idListBox     = 100;

type
  { Application skeleton with menu and command handler }
  TMyApp = object(TApplication)
    procedure InitMenuBar; virtual;
    procedure InitStatusLine; virtual;
    procedure HandleEvent(var Event: TEvent); virtual;
  end;

function NewListBoxDialog: PDialog;
var
  D     : PDialog;
  R, RB : TRect;
  LB    : PListBox;
  SB    : PScrollBar;
  SC    : PStringCollection;
  i     : Integer;
  s     : string;
begin
  { Create a centered dialog window }
  R.Assign(0, 0, 50, 16);
  D := New(PDialog, Init(R, 'TListBox demo'));
  D^.Options := D^.Options or ofCentered;

  { Create the vertical scrollbar on the right side of the list box }
  R.Assign(2, 2, 46, 12);     { List box bounds }
  RB.Assign(46, 2, 47, 12);   { Vertical scrollbar bounds }

  SB := New(PScrollBar, Init(RB));
  D^.Insert(SB);

  { Create the list box and link it with the scrollbar }
  LB := New(PListBox, Init(R, SB));
  LB^.SetId(idListBox);
  D^.Insert(LB);

  { Prepare a collection of strings and attach it to the list box }
  SC := New(PStringCollection, Init(50, 10));
  for i := 1 to 40 do
  begin
    Str(i, s);
    SC^.Insert(NewStr('Item ' + s));
  end;
  LB^.NewList(SC);  { TListBox takes ownership of the collection }

  { OK button }
  R.Assign(14, 13, 26, 15);
  D^.Insert(New(PButton, Init(R, '~O~K', cmOK, bfDefault)));

  { Cancel button }
  R.Assign(28, 13, 40, 15);
  D^.Insert(New(PButton, Init(R, 'Cancel', cmCancel, bfNormal)));

  NewListBoxDialog := D;
end;

procedure TMyApp.InitMenuBar;
var
  R: TRect;
begin
  { Build a simple menu with one command to show the TListBox dialog }
  GetExtent(R);
  R.B.Y := R.A.Y + 1;
  MenuBar := New(PMenuBar, Init(R,
    NewMenu(
      NewSubMenu('~D~emo', hcNoContext,
        NewMenu(
          NewItem('~L~istBox demo', 'F2', kbF2, cmShowListBox, hcNoContext,
          NewLine(
          NewItem('E~x~it', 'Alt-X', kbAltX, cmQuit, hcNoContext, nil)))),
      nil)
  ));
end;

procedure TMyApp.InitStatusLine;
var
  R: TRect;
begin
  { Status line with a few useful hints }
  GetExtent(R);
  R.A.Y := R.B.Y - 1;
  StatusLine := New(PStatusLine, Init(R,
    NewStatusDef(0, $FFFF,
      NewStatusKey('~F2~ ListBox demo', kbF2, cmShowListBox,
      NewStatusKey('~Alt-X~ Exit', kbAltX, cmQuit, nil)),
    nil)
  ));
end;

procedure TMyApp.HandleEvent(var Event: TEvent);
var
  D  : PDialog;
  LB : PListBox;
  idx: Integer;
  ps : PString;
begin
  { Let the base class process standard events first }
  TApplication.HandleEvent(Event);

  if (Event.What = evCommand) then
  begin
    case Event.Command of
      cmShowListBox:
        begin
          D := NewListBoxDialog;
          if Desktop^.ExecView(D) = cmOK then
          begin
            { On OK, show currently focused item (if any) }
            LB := PListBox(D^.Find(idListBox));
            if LB <> nil then
            begin
              idx := LB^.Focused;
              if (idx >= 0) and (LB^.List <> nil) then
              begin
                ps := PString(LB^.List^.At(idx));
                if ps <> nil then
                  MessageBox(ps^, nil, mfInformation + mfOKButton);
              end;
            end;
          end;
          Dispose(D, Done);
          ClearEvent(Event);
        end;
    end;
  end;
end;

var
  MyApp: TMyApp;
begin
  { Standard Turbo Vision application startup sequence }
  MyApp.Init;
  MyApp.Run;
  MyApp.Done;
end.

