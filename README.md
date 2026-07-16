# Dense Runestone Notifier

A RuneLite plugin for training Runecrafting/Crafting at the Dense essence mine
(Isle of Souls, east of the Dark Altar in Arceuus). Dense runestones come in
pairs and deplete randomly while you mine them, and there's no real cue when
it happens. Most people are half-watching another screen while training this,
so it's easy to sit there idling for a while, not realizing your rock died a
minute ago and you've been getting nothing. This plugin watches the rock
you're working and throws up a notification the moment you stop mining it, so
you actually notice and can swap to the other rock.

## Features

- **Mining started** notification (green by default) when you begin actively
  chiselling a dense runestone.
- **Stopped mining** notification (red by default) the moment you're no longer
  working that rock - depleted, inventory full, or anything else that
  interrupted you.
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
