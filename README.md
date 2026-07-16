# Dense Runestone Notifier

A RuneLite plugin for training Runecrafting/Crafting at the Dense essence mine
(Isle of Souls, east of the Dark Altar in Arceuus). Dense runestones come in
pairs and deplete randomly as you mine them, with no vanilla feedback other
than the rock going quiet - it's easy to keep clicking a rock that already
depleted instead of swapping to its pair. This plugin watches the rock you're
actively working and shows a notification above your chatbox the moment you
stop mining it, so you know to switch immediately.

## Features

- **Mining started** notification (green by default) when you begin actively
  chiselling a dense runestone.
- **Stopped mining** notification (red by default) the moment you're no longer
  working that rock - whether it depleted, your inventory filled up, or you
  walked away.
- Notification automatically clears after 5 minutes of inactivity, or once
  you've wandered more than 20 tiles from where you were last mining.
- Fully configurable: toggle on/off, box background colors, text color, and
  the message shown for each state.

## Configuration

| Setting | Default | Description |
|---|---|---|
| Enabled | On | Show notifications when you start/stop mining |
| Text color | `#FFFFFF` | Color of the notification text |
| Start box color | `#00AA00` | Background color for the "mining started" notification |
| Start message | `Mining dense essence` | Text shown when you start mining |
| Stopped box color | `#AD0000` | Background color for the "stopped mining" notification |
| Stopped message | `Not mining dense essence` | Text shown when you're no longer mining |

The notification overlay is a standard RuneLite overlay positioned above the
chatbox by default, and can be dragged anywhere via RuneLite's overlay edit
mode.
