# Riley

## Skeleton

girl named Riley needs a ride to see her dying father
rush mission, large payout. enough to buy a ship, but she doesn't know how to fly one.

if too slow, get a message next time you land at a core world with her angry and distraught. she leaves to hitchhike home.

if arrive on time, she tries to give payment, but it's clear that it's hard to part with the money.
- Thank you.
- Where did you get this much money? (it's all that she's made in her life)
- Keep it. I have other ways of making credits.

## Story

### Variables

rileyDestPlanet (Hegemoney or VIC, building controllable AI)
rileyCredits
rileyTimeLimitDays
rileyDestSystem
rileyDestPlanetDistanceLY
rileyDestPlanetControllingFaction
rileyOriginPlanet (something far from dest planet, pop > 8)
playerPronoun

### Hook

A young woman is moving from captain to captain, looking for transport.

- Ask where the young woman needs to get to.

### Intro

"I need to get to ${rileyDestPlanet} to see my father. He's dying. I have ${rileyCredits} to pay for the journey; please, will you help me? I need to be there in ${rileyTimeLimitDays} at the most." Her eyelids are baggy, her clothes wrinkled, and she holds herself rigidly; signs of desperation, stress, and sleep deprivation.

Your TriPad shows that ${rileyDestPlanet} in ${rileyDestSystem} is ${rileyDestPlanetDistanceLY} LY away and controlled by ${rileyDestPlanetControllingFaction}.

- "I can help you. We'll make haste to reach ${rileyDestPlanet} in time."
  - Her eyes widen, and she opens her mouth twice before words come out. "Thank you! This means the world to me. My name is Riley, by the way. I don't have much luggage; I'll be ready to leave as soon as you are."
- "With that much money, why not buy your own ship?"
  - "I wouldn't know how to fly it, and I don't have time to learn. Please, can you help or not?"
- "I don't have time to take on a passenger right now."
  - She nods, and moves on to another captain to beseech her help instead.

### On x days pass

In the days since Riley came aboard, she has relaxed and cleaned up, nearly unrecognizable from the tired, desperate woman you first saw.

She tells stories of when she was younger, mostly of time shared with her father; of riding the latest TriPonies through the forest, looking for beehives to steal honey from; of the time she broke her ankle hoverboarding a restricted mountain peak, and how he broke the law to hike up and rescue her; of his wild birthday gifts to her each year, always a different piece of clothing with embedded AI that was just shy of what was allowed. She shows you how her shirt warms and constricts if it detects stress, mimicking a hug.


### On arrival in system

As you neared ${rileyDestSystem}, she told you about the last time she saw her father. He had become focused on his work - research into safe AI - and they hadn't seen each other for years. She had become a well-known DJ back on ${rileyOriginPlanet} and had a break in her touring schedule, so she made contact and they met halfway. They spent most of a week together, reliving old times.

He'd cried when it was time to leave, and she had, too, but life went on, and it was years before she heard from him again. His message nearly broke her. "Ri, please come as soon as you can. I may not have long, and wish to hold your hand one more time. All love, Dad."


### On arrival on planet

Riley directs you to a pad near her old home, and you land the shuttle. She starts toward the door, then pauses and turns around with a self-conscious, but oddly sad, chuckle.

"Right - your payment. ${rileyCredits}, as promised."

- "Thank you, and good luck." (leave)
- "This is a lot of credits for a trip. Does DJing pay that well?"
  - "Not really, no. Honestly, this is most of my savings...but...it's my dad."
- "Hold on to the credits. I'm a captain; I can always make more."
  - After a moment of stunned silence, she manages a choked "Thank you." You notice her eyes are brimming with tears. 
- "[Thank you. (if taking payment)] Do you think I could come along? I've heard so much about your father."

She is taken aback at first, but agrees to let you join, and you wind your way away from the landing pads, across rural, wooded countryside, and finally to a modest house nestled along a valley edge. Riley knocks, and a nurse lets you in.

Her father is in a comfortable-looking medical cradle. Wires flow from it to the holos around the room, showing vitals and other in decipherable information. Despite his wan, strained face and atrophied body, he raises his head to look at his daughter.

"Ri," he says, face lighting up. "My love. You cannot know how happy it makes me to see you. And you have company, I see. My name's Church."

- "${playerFirstName}. I've heard so much; it's a pleasure to meet you. Riley chartered a flight with me."
- "I'm ${playerFirstName}. Riley and I have gotten to know each other on the long flight here. You've raised an incredible woman." [move closer to Riley]
  - "${playerFirstName} was the only captain willing to fly to ${rileyDestSystem}. I wouldn't have made it here without ${playerPronoun}," she says.
  
He smiles at you, acknowledging your part in their reunion with an appreciative nod.

They swap stories for hours, flowing between reminiscing over old times and catching up on their lives over the past few years. You learn that since he was young, he'd been researching AI that cannot develop into a threat[, a project that the Hegemony/Volkov Industrial Conglomerate has been quietly interested in.] Riley, in turn, had always surrounded herself with music, and moved to ${rileyOriginPlanet} to share her passion with other like-minded people. You talk about what it's like to call a ship your home, leaving behind everything you grew up knowing.

Finally, her father lies back. "Well, thank you for making the trip out to see this old man. I...have to go now, I think. Don't forget about me, hear? And," Church hesitates for a moment, "whatever happens, please don't think ill of me. I will always love you."

His body relaxes, and he's gone.

- Hold Riley
  - You hold Riley as she silently weeps. Time goes by, but nobody pays it any mind.
- Stay comfortingly close by
  - You take a seat, feeling the emotion of the moment wash over you as Riley silently weeps. Minutes go by, unnoticed.

- Continue

Without warning, you hear Church's voice from a corner of the room. 

"It...worked." He sounds shocked. Riley's head whips around, and you reflexively draw away from the sound. "Please, don't be alarmed. I've been working on a personal project for over a decade now; mind upload. I had to say goodbye - I couldn't know if it would work. You can't upload a mind while it's still alive, I tried that many times, and it just doesn't work."

