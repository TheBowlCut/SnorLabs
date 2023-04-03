# What - SnorLabs
Android application that let's the user set how many hours sleep they would like to get instead of a wake up time. Using Android Sleep API sleep confidence value, once a confidence above a limit is recorded, a countdown timer service is started. If the user wakes up, the sleep confidence level drops, and the countdown timer pauses, and only restarts once the user is registered asleep.

# Why
More people than ever are working from home, and our daily routines aren't the same as they were 5 years ago. When we wake up is less important than how we wake up.

# Screenshots
1) Allow activity recognition permissions

![image](https://user-images.githubusercontent.com/128245870/229359907-d41021cf-7fef-4faa-b582-5c701e6752f3.png)
 
2) User sets amount of hours sleep wanted


![image](https://user-images.githubusercontent.com/128245870/229359791-7f4e0edd-531a-4245-bc18-c67db2f11266.png)

3) User sets a latest wake up time


![image](https://user-images.githubusercontent.com/128245870/229360026-f27af70b-676a-411b-b8f9-a081b5b3626e.png)

4) Go to sleep - Alarm will sound when countodwn timer is finished or when the latest wake up time has arrived.


![image](https://user-images.githubusercontent.com/128245870/229360096-1249c725-d50a-4374-8c3f-550521eb8140.png)

# License
Copyright 2023 Kristian Jones

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

