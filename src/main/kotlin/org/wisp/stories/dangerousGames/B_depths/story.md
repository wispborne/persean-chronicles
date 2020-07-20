# Dangerous Games Pt. 2

"Dangerous Games A" must be finished first.

## Intro

"**FirstName**!" A huge hand claps you on the shoulder and spins you around, where you find yourself facing a large, widly grinning man.
"Remember me? Yer the best pilot I've had - that stunt to get us out of Skymoot, right out of the dragon's nest? Epic."
A crude, but professional, sign behind him reads, 'Dangerous Games - Karengo's Galactic Adventures'.
"Like it?" Karengo follows your gaze. "Decided to make this an official thing. Well...an unofficial official thing.
Feds wouldn't understand. Doesn't matter; I need a pilot again, and a good one. Yer ship's waterproof, right?"
  _"Uh, yeah. I think so."_
  _"Of course."_
  goto cont
  _"Sorry, mate. You'll have to find another pilot."_
  decline

  label cont
Karengo laughs. "It handles the vacuum of space, yeah? Yer right, it'll be fine. I've got a group of lads and ladies here rarin' to see some quoijuu in heat, right up close'n'personal.
They've all got their own subpods, so how's about we load up and head out in the next few hours. Good? Good."
 "Oh, nearly forgot. We're headed to Deadman's Cove, in Almach. Ain't sure who's in charge over there these days, so we might need to grease some palms. Best to keep your eyes open and shields up."

## Arrival at Destination

- Must be habitable
- Must be colonized
- Must contain ocean

As before, on Skymoot, Karengo guides you down to a landing spot far away from the local government (_special message if player owns world_). "Don't want no interruptions," he says.
Despite days of pressuring, you've been unable to get Karengo to tell you exactly what a quoijuu is, other than the slightly ominous, "the biggest fish".
He sends a file to your ship computer and grins. "Since it's too late to back out now, it's time to fill you in.
Quoijuu aren't the only thing down there; you've also got scyllae, taniwhales, morgawrii, plus some hydrothermal vents, none of which will benefit us if we run into them.
The good news is that there is a path to follow. The bad news is that the only person to make the path public was a bit of a nut.
It's all in riddles, and I haven't been able to make head nor tail of them. Look them over and we'll follow your lead. And don't worry too much about it - what's the worst that could happen?"

  _"Ok..."_
  _"Ok, no problem."_

The adventure seekers, who you've spent the trip getting to know, are getting ready. You watch a nearby pair, Daciana and Mussie,
helping each other out to put on wetsuits, when Daciana catches your gaze and grins. "Bafta, mate. Safe sailing, see you on the other side."
Karengo gives you a signal and you hurry back to the cockpit to fly out, away from safe shoals and over the watery abyss.
At your touch, the <ship> slowly sinks below the waves. As Karengo warned you about on the way over, most of your navigation systems quickly flash red warnings and go offline;
there is some unknown interference that overwhelm your poor ship's sensors, which were designed for space first and water second, or fifth, or not at all.
Minutes tick by as the crew silently watches one of the few sensors still working. A young couple, Balazs and Jorma, uneasily exchange glances.
EIGHT KILOMETERS, the depth meter reads. TEN KILOMETERS. SIXTEEN KILOMETERS. A loud, metallic creak echoes through the ship and Mussie jumps, startled.
Karengo claps him on the back and gives him a signature grin. "Maybe <first> can keep you aboard after this in case he needs anything from a high shelf!" Mussie smiles weakly, but his eyes don't leave the readout.
TWENTY-FIVE KILOMETERS. "This's it," Karengo announces, "Sub up! Let's see what there is to see."
The <ship> smoothly descends into the mouth of a massive cave and your visibility, clouded by dark sulfide emissions from the hydrothermal vents at the floor, drops to less than eight meters.
A fan of 10 personal subpods fan out behind you, their communication equipment syncing with yours to display them as blips on your screen.

## Riddle 1

  _(view first riddle)_
"Ok <first>," Karengo crackles over the radio. "Lead on."

No other path may you take
Oiled skins flash and flicker
Razer spikes grow from the deep
Titanic walls crush and smash
Heed the beginnings to live.

  _(move east)_
  goto east-bad-morgawrii
  _(move north)_
  goto north-success
  _(move south)_
  goto south-bad-vents
  _(move west)_
  goto west-bad-wall

  **label east-bad-morgawrii**
Small schools of fish flit past as you guide your small fleet to the east. Out of the darkness, some sort of small whale crashes into your ship, setting off a couple of proximity alarms but causing no damage. It isn't until you hear screaming, distorted by the speakers, that it becomes clear what you just saw; the "small whale" was a just a flipper of something much, much larger.
"Morgawrii!" shouts Karengo, "Turn around! Turn around!"
  _(go back)_

Hauling on the controls, you spin your ship around. Leviathan shapes rise from below, stirring the water around your ship as it speeds to safety.
  goto end-fail

  **label south-bad-vents**
Small schools of fish flit past as you guide your small fleet to the south. The swirling clouds of emissions seem to get thicker and thicker, until there's no reason to look at anything but the ship compass. The screen beeps once and a blip disappears.
"Elias?" Karengo asks. "Your transmitter just cut out."
When there's no response, he swears. "Everybody go back." he broadcasts. "Something's wrong."
  _(go back)_

You spin your ship around and hear a loud crunch come from outside - your shields have just impacted something large and solid. The outer lights shine off falling stone and it suddenly it all comes together; you've led everybody through a field of hydrothermal vents that rise for tens of meters off the bottom, obscured by their own noxious clouds.
You guide your ship back to safety, toppling a couple more underwater smokestacks as you go.
  goto end-fail

  **label west-bad-wall**
Small schools of fish flit past as you guide your small fleet to the west. Minutes pass with no sign of danger, and finally Karengo broadcasts, "Good work, <first>. Looks like this is the way forward."
"Last one there's a rotton fish eye!" shouts Elias, and your screen shows his subpod leap forward and gain on you, followed quickly by others'.
Then, without warning, a proximity sensor goes off at the same time your outer lights show a wall directly in front, stretching for as far as you can see.
"Stop!" shouts Karengo, "It's the end of the cavern! Stop and go back!"
  _(go back)_

Shuttering, your ship comes to a halt mere meters from the endless stone face. With a sinking feeling that has nothing to do with the depth, you turn the ship around and head back the way you came.
  goto end-fail

  **label north-success**
Small schools of fish flit past as you guide your small fleet to the north. Minutes pass with no sign of danger, and finally Karengo broadcasts, "Good work, <first>. Looks like this is the way forward."
"Last one there's a rotton fish eye!" shouts Elias, and your screen shows his subpod leap forward and gain on you, followed quickly by others'.
"Woah!" says Karengo, over the radio. "I think that's far enough. Which way to we go next, <first>?"
  _(open the next riddle)_

### On Choice 1 Failure

A quick roll call shows two subpods missing; Elias and Taddese never made it back.
A voice crackles over the radio, "It's clear up this way!" Somebody, no doubt unimpressed by your navigating, has scouted ahead and found a way though.

## Riddle 2

There are stingers in the deep
A certain death, a dreamless sleep.
Go to beast that has no bee
No danger there, just open sea.

## The Crew

- Elias (death at choice 1 failure)
- Taddese (death at choice 1 failure)
- Daciana
- Mussie
- Balazs
- Jorma

## the Riddles

No other path may you take
Oiled skins flash and flicker
Razer spikes grow from the deep
Titanic walls crush and smash
Heed the beginnings to live.

There are stingers in the deep
A certain death, a dreamless sleep.
Go to beast that has no bee
No danger there, just open sea.

To the north and to the east
Track my movements like a beast.
A quick turn right and forward on,
Come toward me where danger's gone.